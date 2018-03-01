package com.enonic.lib.license;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.branch.Branch;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.home.HomeDir;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.User;
import com.enonic.xp.security.auth.AuthenticationInfo;

@Component(immediate = true)
public final class LicenseManagerImpl
    implements LicenseManager
{
    static final int KEY_SIZE = 2048;

    private static final String LICENSE_HEADER = "LICENSE";

    private static final String LIC_FILE_EXT = ".lic";

    private static final String LICENSE_DIR = "license";

    public static final RepositoryId REPO_ID = RepositoryId.from( "com.enonic.licensemanager" );

    private static final String INSTALLED_LICENSES = "installed-licenses";

    public static final NodePath INSTALLED_LICENSES_PATH = NodePath.create( NodePath.ROOT, INSTALLED_LICENSES ).build();

    public static final String NODE_LICENSE_PROPERTY = "license";

    private NodeService nodeService;

    private ResourceService resourceService;

    private ApplicationKey currentApp;

    public LicenseManagerImpl()
    {
        try
        {
            currentApp = ApplicationKey.from( getClass() );
        }
        catch ( NullPointerException e )
        {
            currentApp = null;
        }
    }

    @Override
    public KeyPair generateKeyPair()
    {
        final KeyPairGenerator keyGen = getRSAKeyPairGenerator();
        keyGen.initialize( KEY_SIZE );
        final java.security.KeyPair rsaKeyPair = keyGen.genKeyPair();
        return new KeyPair( rsaKeyPair );
    }

    public String generateLicense( final com.enonic.lib.license.PrivateKey privateKey, final LicenseDetails license )
    {
        String licenseData = LicenseDetailsJSONConverter.serialize( license );
        licenseData = Base64.getEncoder().withoutPadding().encodeToString( licenseData.getBytes( StandardCharsets.UTF_8 ) );

        final String signature = sign( licenseData, privateKey.getRsaKey() );
        return FormatHelper.asPEM( licenseData + "." + signature, LICENSE_HEADER );
    }

    @Override
    public LicenseDetails validateLicense( final com.enonic.lib.license.PublicKey publicKey, final String license )
    {
        final String licenseStr = unwrapLicense( license );
        final int p = licenseStr.indexOf( '.' );
        if ( p == -1 )
        {
            return null;
        }
        final String licenseData = licenseStr.substring( 0, p );
        final String signature = licenseStr.substring( p + 1 );

        final boolean valid = verify( licenseData, signature, publicKey.getRsaKey() );
        if ( !valid )
        {
            return null;
        }

        try
        {
            final byte[] licenseDataBytes = Base64.getUrlDecoder().decode( licenseData );
            final String licenseDataJson = new String( licenseDataBytes, StandardCharsets.UTF_8 );

            return LicenseDetailsJSONConverter.parse( licenseDataJson );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    @Override
    public LicenseDetails validateLicense( String appKey )
    {
        final ApplicationKey app = this.currentApp;
        final Resource pubKeyRes = this.resourceService.getResource( ResourceKey.from( app, "/app.pub" ) );
        if ( !pubKeyRes.exists() )
        {
            return null;
        }

        String publicKeyStr = pubKeyRes.readString();
        final com.enonic.lib.license.PublicKey publicKey = com.enonic.lib.license.PublicKey.from( publicKeyStr );
        if ( publicKey == null )
        {
            return null;
        }

        if ( appKey == null )
        {
            appKey = app.toString();
        }

        String license = readLicenseFile( appKey );
        if ( license == null )
        {
            license = readLicenseFromRepo( appKey );
        }

        if ( license == null )
        {
            return null;
        }

        return this.validateLicense( publicKey, license );
    }

    private String unwrapLicense( final String license )
    {
        if ( license == null )
        {
            return "";
        }
        final String allLines[] = license.split( "\\r?\\n" );
        final List<String> lines = Arrays.stream( allLines ).
            map( String::trim ).
            filter( s -> !s.isEmpty() ).
            collect( Collectors.toList() );
        if ( lines.size() < 3 )
        {
            return "";
        }

        final String header = lines.get( 0 );
        final String footer = lines.get( lines.size() - 1 );
        lines.remove( 0 );
        lines.remove( lines.size() - 1 );
        if ( !FormatHelper.isPEMHeader( header, LICENSE_HEADER ) || !FormatHelper.isPEMFooter( footer, LICENSE_HEADER ) )
        {
            return "";
        }

        return lines.stream().collect( Collectors.joining( "" ) );
    }

    private String sign( String plainText, PrivateKey privateKey )
    {
        try
        {
            final Signature privateSignature = Signature.getInstance( "SHA256withRSA" );
            privateSignature.initSign( privateKey );
            privateSignature.update( plainText.getBytes( StandardCharsets.UTF_8 ) );

            final byte[] signature = privateSignature.sign();
            return Base64.getEncoder().withoutPadding().encodeToString( signature );
        }
        catch ( GeneralSecurityException e )
        {
            throw new RuntimeException( e );
        }
    }

    private boolean verify( String plainText, String signature, PublicKey publicKey )
    {
        try
        {
            Signature publicSignature = Signature.getInstance( "SHA256withRSA" );
            publicSignature.initVerify( publicKey );
            publicSignature.update( plainText.getBytes( StandardCharsets.UTF_8 ) );

            byte[] signatureBytes = Base64.getDecoder().decode( signature );

            return publicSignature.verify( signatureBytes );
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    private KeyPairGenerator getRSAKeyPairGenerator()
    {
        try
        {
            return KeyPairGenerator.getInstance( "RSA" );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }
    }

    private String readLicenseFile( final String appKey )
    {
        final String fileName = appKey + LIC_FILE_EXT;
        final Path xpHome = HomeDir.get().toFile().toPath();
        final Path licenseDir = xpHome.resolve( LICENSE_DIR );
        final Path licensePath = licenseDir.resolve( fileName );
        if ( !licensePath.toFile().isFile() )
        {
            return null;
        }
        try
        {
            return new String( Files.readAllBytes( licensePath ), StandardCharsets.UTF_8 );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    private String readLicenseFromRepo( final String appKey )
    {
        final Context ctxRepo = ContextBuilder.from( ContextAccessor.current() ).
            repositoryId( REPO_ID ).
            branch( Branch.from( "master" ) ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.ADMIN ).user( User.ANONYMOUS ).build() ).
            build();
        return ctxRepo.callWith( () -> loadLicense( appKey ) );
    }

    private String loadLicense( final String appKey )
    {
        try
        {
            final NodePath path = NodePath.create( INSTALLED_LICENSES_PATH, appKey ).build();
            final Node licenseNode = nodeService.getByPath( path );
            if ( licenseNode == null )
            {
                return null;
            }
            return licenseNode.data().getString( NODE_LICENSE_PROPERTY );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    public void setCurrentApp( final ApplicationKey currentApp )
    {
        this.currentApp = currentApp;
    }

    @Reference
    public void setNodeService( final NodeService nodeService )
    {
        this.nodeService = nodeService;
    }

    @Reference
    public void setResourceService( final ResourceService resourceService )
    {
        this.resourceService = resourceService;
    }
}

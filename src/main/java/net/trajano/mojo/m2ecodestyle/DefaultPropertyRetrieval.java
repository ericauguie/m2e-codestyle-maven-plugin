package net.trajano.mojo.m2ecodestyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.io.URLInputStreamFacade;
import org.sonatype.plexus.build.incremental.BuildContext;

@Singleton
@Named
public class DefaultPropertyRetrieval implements PropertyRetrieval {

    /**
     * Build context.
     */
    @Inject
    private BuildContext buildContext;

    /**
     * Fetch and merge the preferences file.
     *
     * @param codeStyleBaseUri
     *            base URI
     * @param prefsFile
     *            prefs file being processed
     * @param destDir
     *            destination directory containing the prefs file.
     * @throws URISyntaxException
     */
    @Override
    public void fetchAndMerge(final URI codeStyleBaseUri,
        final String prefsFile,
        final File destDir) throws URISyntaxException, IOException {

        final File destFile = new File(destDir, prefsFile);
        final Properties props = new Properties();
        if (destFile.exists()) {
            final FileInputStream fileInputStream = new FileInputStream(destFile);
            props.load(fileInputStream);
            fileInputStream.close();
        }

        final InputStream prefsInputStream = openPreferenceStream(codeStyleBaseUri, prefsFile);

        if (prefsInputStream == null) {
            return;
        }
        props.load(prefsInputStream);
        prefsInputStream.close();

        final OutputStream outputStream = buildContext.newFileOutputStream(destFile);
        props.store(outputStream, "Generated by m2e codestyle maven plugin");
        outputStream.close();
    }

    /**
     * Create an input stream pointing to the prefs file inside the code style
     * base URI.
     *
     * @param codeStyleBaseUri
     * @param prefsFile
     * @return stream or <code>null</code> if the target is not available.
     * @throws MalformedURLException
     * @throws IOException
     */
    @Override
    public InputStream openPreferenceStream(final URI codeStyleBaseUri,
        final String prefsFile) throws MalformedURLException, IOException {

        final URI resolved = codeStyleBaseUri.resolve(prefsFile);
        try {
            if (resolved.isAbsolute()) {
                return new URLInputStreamFacade(resolved.toURL()).getInputStream();
            } else {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(resolved.toString());
            }
        } catch (final FileNotFoundException e) {
            return null;
        }
    }
}

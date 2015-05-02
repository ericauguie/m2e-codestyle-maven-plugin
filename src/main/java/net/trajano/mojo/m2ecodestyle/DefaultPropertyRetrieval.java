package net.trajano.mojo.m2ecodestyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.io.URLInputStreamFacade;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Default implementation of {@link PropertyRetrieval}
 */
@Singleton
@Named
public class DefaultPropertyRetrieval implements PropertyRetrieval {

    /**
     * Build context.
     */
    @Inject
    private BuildContext buildContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void fetchAndMerge(final URI codeStyleBaseUri,
            final String prefsFile,
            final File destDir) throws IOException {

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
     * Performs the actual work of getting the stream.
     *
     * @param resolved
     *            resolved URI
     * @return stream or <code>null</code> if the target is not available.
     * @throws IOException
     *             I/O error
     */
    private InputStream internalOpenStream(final URI resolved) throws IOException {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream openPreferenceStream(final URI codeStyleBaseUri,
            final String prefsFile) throws IOException {

        final URI resolved = codeStyleBaseUri.resolve(prefsFile);
        return internalOpenStream(resolved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream openStream(final String url) throws IOException {

        final URI resolved = URI.create(url);
        return internalOpenStream(resolved);
    }
}

package org.tortiepoint.pachyderm.dependency;

import org.apache.log4j.Logger;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class DependencyResolver {

    private static Logger log = Logger.getLogger(DependencyResolver.class);
    private RepositorySystem repositorySystem;
    private List<RemoteRepository> remoteRepositories = new ArrayList<RemoteRepository>();
    private LocalRepository localRepository =
            new LocalRepository(System.getProperty("user.home") + "/.pachyderm/repo");

    {
        remoteRepositories.add(new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/"));
    }

    public DependencyResolver() throws PlexusContainerException, ComponentLookupException {
        this.repositorySystem = new DefaultPlexusContainer().lookup( RepositorySystem.class );
    }

    public void addDependency(String groupId, String artifactId, String extension, String version) throws DependencyResolutionException {
        try {
            resolveDependency(new Dependency(new DefaultArtifact(groupId, artifactId, extension, version), "runtime"));
        } catch (Exception e) {
            throw new DependencyResolutionException(String.format("Error resolving dependency %s:%s:%s",
                    groupId, artifactId, version), e);
        }
    }

    public void addDependency(String coords) throws DependencyResolutionException {
        try {
            resolveDependency(new Dependency(new DefaultArtifact(coords), "runtime"));
        } catch (Exception e) {
            throw new DependencyResolutionException(String.format("Error resolving dependency %s",
                    coords), e);
        }
    }

    public void addRemoteRepository(String id, String url) {
        remoteRepositories.add(new RemoteRepository(id, "default", url));
    }

    private void resolveDependency(Dependency dependency) throws Exception {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        URLClassLoader classLoader = (URLClassLoader) this.getClass().getClassLoader();

        session.setLocalRepositoryManager(this.repositorySystem.newLocalRepositoryManager(this.localRepository));

        CollectRequest collectRequest = new CollectRequest();

        collectRequest.setRoot(dependency);

        for (RemoteRepository remoteRepository : this.remoteRepositories) {
            collectRequest.addRepository(remoteRepository);
        }

        DependencyNode node = this.repositorySystem.collectDependencies(session, collectRequest).getRoot();
        DependencyRequest dependencyRequest = new DependencyRequest(node, null);
        DependencyResult dependencyResult = this.repositorySystem.resolveDependencies(session, dependencyRequest);

        for (ArtifactResult artifactResult : dependencyResult.getArtifactResults()) {
            Artifact artifact = artifactResult.getArtifact();
            log.info(String.format("Resolved %s:%s:%s", artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion()));
            addSoftwareLibrary(artifactResult.getArtifact().getFile());
        }
    }

    private static void addSoftwareLibrary(File file) throws Exception {
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{file.toURI().toURL()});
    }
}
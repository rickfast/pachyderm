package org.tortiepoint.pachyderm.dependency;

import org.apache.maven.repository.internal.*;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rickfast
 * Date: 9/10/12
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class DependencyResolver {

    private RepositorySystem repositorySystem;
    private List<RemoteRepository> remoteRepositories = new ArrayList<RemoteRepository>();
    private LocalRepository localRepository =
            new LocalRepository(System.getProperty("user.home") + "/.pachyderm/repo");

    {
        remoteRepositories.add(new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/"));
    }

    public DependencyResolver() {
        DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);

//        locator.addService(VersionRangeResolver.class, DefaultVersionRangeResolver.class);
//        locator.addService(VersionResolver.class, DefaultVersionResolver.class);
//        locator.addService(ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);

        this.repositorySystem = locator.getService(RepositorySystem.class);
    }


    public List<RemoteRepository> getRemoteRepositories() {
        return remoteRepositories;
    }

    public LocalRepository getLocalRepository() {
        return localRepository;
    }

    public void addDependency(String groupId, String artifactId, String extension, String version) {
        try {
            resolveDependency(new Dependency(new DefaultArtifact(groupId, artifactId, extension, version), "runtime"));
        } catch (Exception e) {
            e.printStackTrace();
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
            System.out.println(String.format("Resolved %s:%s:%s", artifact.getGroupId(),
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
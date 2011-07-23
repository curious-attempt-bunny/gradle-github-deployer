package com.curiousattemptbunny.gradle.githubdeployer;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.plugins.resolver.URLResolver;

import java.io.File;
import java.io.IOException;

class GithubDeployer extends URLResolver {
    public static final String NAME = "GitHub Deployer"
    String userName
    String password

    GithubDeployer() {
        setName(NAME);
    }

    @Override
    void publish(Artifact artifact, File src, boolean overwrite) throws IOException {
        println userName
        println password

        println artifact
        println file
        println overwrite

        super.publish(artifact, src, overwrite);
    }
}

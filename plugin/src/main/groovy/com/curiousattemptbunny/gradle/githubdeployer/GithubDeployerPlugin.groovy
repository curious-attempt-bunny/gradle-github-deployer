package com.curiousattemptbunny.gradle.githubdeployer

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler


class GithubDeployerPlugin implements Plugin<Project> {
    void apply(Project project) {
        DefaultRepositoryHandler.metaClass.getGithubDeployer = { ->
            println "accessor"

            def repositoryHandler = delegate

            try {
                repositoryHandler[GithubDeployer.NAME]
            } catch (UnknownRepositoryException) {
                repositoryHandler.add new GithubDeployer()
            }

            return repositoryHandler[GithubDeployer.NAME]
        }
        DefaultRepositoryHandler.metaClass.githubDeployer = { config ->
            println "configuration"

            def deployer = delegate.githubDeployer

            if (config) {
                ConfigureUtil.configure(config, deployer)
            }
        }
    }
}

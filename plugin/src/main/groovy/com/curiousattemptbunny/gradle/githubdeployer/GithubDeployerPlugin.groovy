package com.curiousattemptbunny.gradle.githubdeployer

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler


class GithubDeployerPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply(plugin:'base')

        project.metaClass.githubDeployer = { config ->
            uploadArchives.repositories.add new GithubDeployer(config)
        }
    }
}

package com.curiousattemptbunny.gradle.githubdeployer;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.plugins.resolver.URLResolver;

import java.io.File;
import java.io.IOException
import org.gradle.api.Project
import groovy.json.JsonBuilder
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext
import org.apache.http.HttpRequest
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.entity.StringEntity
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.entity.mime.content.FileBody

class GithubDeployer extends URLResolver {
    public static final String NAME = "GitHub Deployer"
    String userName
    String password

    GithubDeployer() {
        setName(NAME);
    }

    @Override
    void publish(Artifact artifact, File src, boolean overwrite) throws IOException {
        if (!userName || !password) {
            throw new IllegalStateException("Define GithubDeployer username and password in order to be able to deploy to GitHub")
        }

        if (src.name.endsWith('jar')) return;

//        println artifact.attributes

        def githubUrl = "https://api.github.com/repos/$userName/$artifact.attributes.module/downloads"
        def artifactName = src.name
        if (artifactName.endsWith("ivy.xml")) {
            artifactName = "${artifact.attributes.module}-ivy-${artifact.attributes.revision}.xml"
        }

        println "$artifactName: $githubUrl"

        HttpClient client = new DefaultHttpClient();

        String auth = "$userName:$password"
        client.addRequestInterceptor(new HttpRequestInterceptor() {
            void process(HttpRequest httpRequest, HttpContext httpContext) {
                httpRequest.addHeader('Authorization', 'Basic ' + auth.toString().bytes.encodeBase64().toString())
            }
        })

        HttpPost githubPost = new HttpPost(githubUrl);

        def json = new JsonBuilder()

        json {
            name artifactName+System.currentTimeMillis()
            size src.length()
        }

        def document = json.toString()

        githubPost.entity = new StringEntity(document, "application/json", "UTF-8")

        def response = client.execute(githubPost)

        if ((int)(response?.statusLine?.statusCode / 100) != 2) {
            throw new GradleException("Incorrect userName, password? Don't own a GitHub repository called ${artifact.attributes.module}? Response for $githubUrl POST is: $response")
        }

        def upload = new JsonSlurper().parseText(response.entity.content.text)

        println upload.collect { it }.join('\n')

        def map = [key: upload.path,
                acl: upload.acl,
                success_action_status: 201,
                Filename: upload.name,
                AWSAccessKeyId: upload.accesskeyid,
                Policy: upload.policy,
                Signature: upload.signature,
                'Content-Type': upload.mime_type
                , file: src.text
                ]

        HttpPost s3Post = new HttpPost(upload.s3_url)

        MultipartEntity reqEntity = new MultipartEntity()
        map.each { key, value ->
            reqEntity.addPart(key, new StringBody(value as String))
        }
//        reqEntity.addPart("file", new FileBody(src))

        s3Post.entity = reqEntity

        response = client.execute(s3Post)

        if (response?.statusLine?.statusCode != 201) {
            throw new GradleException("Failed to upload GitHub artifact. Response for $upload.s3_url POST is: $response. Body is ${response?.entity?.content?.text}")
        }

        println "Success!!"
    }


}

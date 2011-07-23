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

        assert response?.statusLine?.statusCode / 100 == 2, "Incorrect userName, password? Don't own a GitHub repository called ${artifact.attributes.module}? Response for $githubUrl POST is: $response"

//        def path = "/repos/$githubUser/$githubRepo/downloads"
//
//        def upload = http.request( POST, JSON ) {
//            uri.path = path
//            body = document
//        };
//
//        PostMethod s3Post = new PostMethod(upload.s3_url);
//        Part[] parts = map.collect { new StringPart(it.key, it.value.toString()) } as Part[];
//        s3Post.setRequestEntity(
//            new MultipartRequestEntity(parts, s3Post.getParams())
//            );
//        int status = client.executeMethod(s3Post);
//        println status

    }


}

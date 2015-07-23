package com.anli.generalization.data.utils;

import java.io.File;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class CommonDeployment {

    public static WebArchive getDeployment() {
        File[] dependencies = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        JavaArchive testArchive = ShrinkWrap.create(JavaArchive.class, "jpa-generic-data.jar")
                .addPackages(true, "com/anli/generalization/data")
                .addAsResource("META-INF/persistence-template.xml", "META-INF/persistence.xml")
                .addAsResource("spring/jpa-generic-data-app.xml", "spring/jpa-generic-data-app.xml");
        return ShrinkWrap.create(WebArchive.class, "jpa-generic-data-test.war")
                .addAsLibrary(testArchive)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(dependencies);
    }
}

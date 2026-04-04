plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.h2database:h2:2.2.224")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.4")

    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
    implementation("org.apache.tomcat.embed:tomcat-embed-core:9.0.80")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper:9.0.80")
}

tasks.test {
    useJUnitPlatform()
}
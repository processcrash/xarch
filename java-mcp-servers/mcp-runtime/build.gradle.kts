plugins {
    `java-library`
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.0")
    api("org.slf4j:slf4j-api:2.0.16")
    api("org.slf4j:slf4j-simple:2.0.16")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.assertj:assertj-core:3.26.3")
}

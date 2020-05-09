plugins { 
  java
  eclipse
  id("org.springframework.boot") version "2.2.1.RELEASE" apply false
}

   group = "it.discovery"

   apply(plugin = "java")
   apply(plugin = "org.springframework.boot")

   java.sourceCompatibility = JavaVersion.VERSION_12
   java.targetCompatibility = JavaVersion.VERSION_12

   repositories {
     jcenter()
   }

   var springBootVersion = "2.2.1.RELEASE"
   
   dependencies {
        implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
        compile("org.springframework.boot:spring-boot-starter-web")

        runtime("javax.annotation:javax.annotation-api:1.3.1")
        compileOnly("org.projectlombok:lombok:1.18.10")
        annotationProcessor("org.projectlombok:lombok:1.18.10")
   } 


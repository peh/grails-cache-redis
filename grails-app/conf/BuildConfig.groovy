grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for gh-pages branch
grails.project.source.level = 1.6

grails.project.dependency.resolver = "ivy"
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile 'redis.clients:jedis:2.4.2'
		compile 'org.springframework.data:spring-data-redis:1.3.0.RELEASE'
        compile 'org.springframework:spring-expression:4.0.2.RELEASE'

    }

	plugins {
        build(":release:3.0.1", ":rest-client-builder:1.0.3") {
            export = false
        }
		compile(':cache:1.1.6')
	}
}

/* Copyright 2012 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import grails.plugin.cache.redis.GrailsRedisCacheManager
import grails.plugin.cache.web.filter.redis.*
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.data.redis.cache.DefaultRedisCachePrefix
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisShardInfo
import redis.clients.jedis.Protocol

class CacheRedisGrailsPlugin {

    String version = '1.1.0'
    String grailsVersion = '2.0 > *'
    def loadAfter = ['cache']
    def pluginExcludes = [
            'grails-app/conf/*CacheConfig.groovy',
            'scripts/CreateCacheRedisTestApps.groovy',
            'docs/**',
            'src/docs/**'
    ]

    String title = 'Redis Cache Plugin'
    String author = 'Burt Beckwith'
    String authorEmail = 'beckwithb@vmware.com'
    String description = 'A Redis-based implementation of the Cache plugin'
    String documentation = 'http://grails.org/plugin/cache-redis'

    String license = 'APACHE'
    def organization = [name: 'SpringSource', url: 'http://www.springsource.org/']
    def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPCACHEREDIS']
    def scm = [url: 'https://github.com/grails-plugins/grails-cache-redis']

    def doWithSpring = {
        if (!isEnabled(application)) {
            log.warn 'Redis Cache plugin is disabled'
            return
        }

        def cacheConfig = application.config.grails.cache
        def redisCacheConfig = cacheConfig.redis
        int configuredDatabase = redisCacheConfig.database ?: 0
        boolean configuredUsePool = (redisCacheConfig.usePool instanceof Boolean) ? redisCacheConfig.usePool : true
        String configuredHostName = redisCacheConfig.hostName ?: 'localhost'
        int configuredPort = redisCacheConfig.port ?: Protocol.DEFAULT_PORT
        int configuredTimeout = redisCacheConfig.timeout ?: Protocol.DEFAULT_TIMEOUT
        String configuredPassword = redisCacheConfig.password ?: null

        grailsCacheJedisPoolConfig(JedisPoolConfig)

        grailsCacheJedisShardInfo(JedisShardInfo, configuredHostName, configuredPort) {
            password = configuredPassword
            timeout = configuredTimeout
        }

        grailsCacheJedisConnectionFactory(JedisConnectionFactory) {
            usePool = configuredUsePool
            database = configuredDatabase
            hostName = configuredHostName
            port = configuredPort
            timeout = configuredTimeout
            password = configuredPassword
            poolConfig = ref('grailsCacheJedisPoolConfig')
            shardInfo = ref('grailsCacheJedisShardInfo')
        }

        grailsRedisCacheSerializer(GrailsSerializer)

        grailsRedisCacheDeserializer(GrailsDeserializer)

        grailsRedisKeySerializer(GrailsKeySerializer)

        grailsRedisCacheDeserializingConverter(GrailsDeserializingConverter) {
            deserializer = ref('grailsRedisCacheDeserializer')
        }

        grailsRedisCacheSerializingConverter(GrailsSerializingConverter) {
            serializer = ref('grailsRedisCacheSerializer')
        }

        grailsCacheRedisSerializer(GrailsRedisSerializer) {
            serializer = ref('grailsRedisCacheSerializingConverter')
            deserializer = ref('grailsRedisCacheDeserializingConverter')
        }

        grailsCacheRedisTemplate(RedisTemplate) {
            connectionFactory = ref('grailsCacheJedisConnectionFactory')
            defaultSerializer = ref('grailsCacheRedisSerializer')
            keySerializer = ref("grailsRedisKeySerializer")
        }

        String delimiter = redisCacheConfig.cachePrefixDelimiter ?: ':'
        redisCachePrefix(DefaultRedisCachePrefix, delimiter)

        grailsCacheManager(GrailsRedisCacheManager, ref('grailsCacheRedisTemplate')) {
            cachePrefix = ref('redisCachePrefix')
        }

        grailsCacheFilter(RedisPageFragmentCachingFilter) {
            cacheManager = ref('grailsCacheManager')
            nativeCacheManager = ref('grailsCacheRedisTemplate')
            // TODO this name might be brittle - perhaps do by type?
            cacheOperationSource = ref('org.springframework.cache.annotation.AnnotationCacheOperationSource#0')
            keyGenerator = ref('webCacheKeyGenerator')
            expressionEvaluator = ref('webExpressionEvaluator')
        }
    }

    private boolean isEnabled(GrailsApplication application) {
        application.config.grails.cache.enabled
    }
}

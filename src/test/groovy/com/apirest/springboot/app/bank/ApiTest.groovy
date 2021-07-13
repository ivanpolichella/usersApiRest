package com.apirest.springboot.app.bank;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.apirest.springboot.app.bank.controllers.UserController;
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )
import groovyx.net.http.RESTClient;
import spock.lang.*;

@SpringBootTest(classes = UserController.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles(value = "test")
@ContextConfiguration
class ApiTest extends Specification {

	static String testURL = "http://localhost:8080"

	RESTClient restClient = new RESTClient(testURL)
	def'User should be able to perform Read Request'(){
		
        setup:
        def testUserId = createTestUser().responseData

        when:
        def response = restClient.get( path: '/usuarios')

        then:
        response.status == 200

        and:
        response.responseData.id
        response.responseData.name
        response.responseData.email
        response.responseData.password
        response.responseData.createdDate
        response.responseData.enabled
        response.responseData.lastModified

        cleanup:
        deleteTestUser(testUserId)

    }
	
	def createTestUser() {
		def requestBody = [name: "Ivan Polichella",
			email: "ivan_pb_bsso@hotmail.com",
			password: "Ivancete123",
			phones: [
				[
					number:"6150615",
					citycode:"221",
					countrycode:"54"
				],
				[
					number:"6003233",
					citycode:"221",
					countrycode:"54"
				]
			]
		]
		return restClient.post(path: '/usuario', body: requestBody, requestContentType: 'application/json')
	}

	def deleteTestUser(def userId) {
		return restClient.delete(path: '/usuario/' + userId)

	}
	
//  def "maximum of two numbers"() {
//    expect:
//    Math.max(a, b) == c
//
//    where:
//    a << [3, 5, 9]
//    b << [7, 4, 9]
//    c << [7, 5, 9]
//  }
//
//  def "minimum of #a and #b is #c"() {
//    expect:
//    Math.min(a, b) == c
//
//    where:
//    a | b || c
//    3 | 7 || 3
//    5 | 4 || 4
//    9 | 9 || 9
//  }
//
//  def "#person.name is a #sex.toLowerCase() person"() {
//    expect:
//    person.getSex() == sex
//
//    where:
//    person                    || sex
//    new Person(name: "Fred")  || "Male"
//    new Person(name: "Wilma") || "Female"
//  }
//
//  static class Person {
//    String name
//    String getSex() {
//      name == "Fred" ? "Male" : "Female"
//    }
//  }
}

package com.example.utexample;

import com.example.utexample.api.RestApi;
import com.example.utexample.service.CountryServiceImpl;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static io.restassured.RestAssured.*;

@RunWith(MockitoJUnitRunner.class)
public class RestApiTest  {
    @Mock
    private CountryServiceImpl countryServiceImpl;
    @InjectMocks
    private RestApi restApi;

    @Before
    public void init(){
        RestAssuredMockMvc.standaloneSetup(restApi);
    }
    @Test
    public void whenRequestGet_thenOK(){
        when().get("/country/2")
                .then().statusCode(200);
    }

}

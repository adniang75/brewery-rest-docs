package guru.springframework.msscbrewery.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.msscbrewery.services.BeerService;
import guru.springframework.msscbrewery.web.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith( { RestDocumentationExtension.class, SpringExtension.class } )
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.brewery.com", uriPort = 80)
@WebMvcTest( BeerController.class )
@ComponentScan( basePackages = "guru.springframework.msscbrewery.web.mappers" )
public class BeerControllerTest {

    @MockBean
    BeerService beerService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    BeerDto validBeer;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setUp ( RestDocumentationContextProvider restDocumentation ) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup( context )
                .apply( documentationConfiguration( restDocumentation ) ).build();
    }

    @BeforeEach
    public void setUp () {
        validBeer = BeerDto.builder().id( UUID.randomUUID() )
                .beerName( "Beer1" )
                .beerStyle( "PALE_ALE" )
                .upc( 123456789012L )
                .build();
    }

    @Test
    public void getBeer () throws Exception {
        given( beerService.getBeerById( any( UUID.class ) ) ).willReturn( validBeer );

        mockMvc.perform( get( "/api/v1/beer/{beerId}", validBeer.getId() )
                        .accept( MediaType.APPLICATION_JSON )
                )
                .andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_UTF8 ) )
                .andExpect( jsonPath( "$.id", is( validBeer.getId().toString() ) ) )
                .andExpect( jsonPath( "$.beerName", is( "Beer1" ) ) )
                .andDo(
                        document(
                                "v1/beer-get-by-id",
                                pathParameters(
                                        parameterWithName( "beerId" ).description( "The id of the beer" )
                                ),
                                responseFields(
                                        fieldWithPath( "id" ).description( "The id of the beer" ).type( "UUID" ),
                                        fieldWithPath( "beerName" ).description( "The name of the beer" ).type( "String" ),
                                        fieldWithPath( "beerStyle" ).description( "The style of the beer" ).type( "String" ),
                                        fieldWithPath( "upc" ).description( "The UPC of the beer" ).type( "Long" ),
                                        fieldWithPath( "createdDate" ).description( "The date the beer was created" ).type( "OffsetDateTime" ),
                                        fieldWithPath( "lastUpdatedDate" ).description( "The date the beer was last updated" ).type( "OffsetDateTime" )
                                )
                        )
                );
    }

    @Test
    public void handlePost () throws Exception {
        //given
        BeerDto beerDto = validBeer;
        beerDto.setId( null );
        BeerDto savedDto = BeerDto.builder().id( UUID.randomUUID() ).beerName( "New Beer" ).build();
        String beerDtoJson = objectMapper.writeValueAsString( beerDto );
        ConstrainedFields fields = new ConstrainedFields( BeerDto.class );
        given( beerService.saveNewBeer( any() ) ).willReturn( savedDto );

        mockMvc
                .perform(
                        post( "/api/v1/beer/" )
                                .contentType( MediaType.APPLICATION_JSON )
                                .content( beerDtoJson ) )
                .andExpect( status().isCreated() )
                .andDo(
                        document(
                                "v1/beer-post-new",
                                requestFields(
                                        fields.withPath( "id" ).ignored(),
                                        fields.withPath( "beerName" ).description( "The name of the beer" ),
                                        fields.withPath( "beerStyle" ).description( "The style of the beer" ),
                                        fields.withPath( "upc" ).description( "The UPC of the beer" ),
                                        fields.withPath( "createdDate" ).ignored(),
                                        fields.withPath( "lastUpdatedDate" ).ignored()
                                )
                        )
                );

    }

    @Test
    public void handleUpdate () throws Exception {
        //given
        BeerDto beerDto = validBeer;
        beerDto.setId( null );
        String beerDtoJson = objectMapper.writeValueAsString( beerDto );
        ConstrainedFields fields = new ConstrainedFields( BeerDto.class );
        //when
        mockMvc
                .perform(
                        put( "/api/v1/beer/{beerId}", UUID.randomUUID() )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( beerDtoJson ) )
                .andExpect( status().isNoContent() )
                        .andDo(
                                document(
                                        "v1/beer-put",
                                        pathParameters(
                                                parameterWithName( "beerId" ).description( "The id of the beer" )
                                        ),
                                        requestFields(
                                                fields.withPath( "id" ).ignored(),
                                                fields.withPath( "beerName" ).description( "The name of the beer" ),
                                                fields.withPath( "beerStyle" ).description( "The style of the beer" ),
                                                fields.withPath( "upc" ).description( "The UPC of the beer" ),
                                                fields.withPath( "createdDate" ).ignored(),
                                                fields.withPath( "lastUpdatedDate" ).ignored()
                                        )
                                )
                        );

        then( beerService ).should().updateBeer( any(), any() );

    }

    @SuppressWarnings( "unused" )
    private static class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields ( Class<?> input ) {
            this.constraintDescriptions = new ConstraintDescriptions( input );
        }

        private FieldDescriptor withPath ( String path ) {
            return fieldWithPath( path )
                    .attributes(
                            key( "constraints" )
                                    .value( StringUtils
                                            .collectionToDelimitedString( this.constraintDescriptions
                                                    .descriptionsForProperty( path ), ". " )
                                    )
                    );
        }
    }
}
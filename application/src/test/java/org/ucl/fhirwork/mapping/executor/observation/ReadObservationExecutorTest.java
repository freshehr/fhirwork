/*
 * FHIRWork (c) 2018 - Blair Butterworth, Abdul-Qadir Ali, Xialong Chen,
 * Chenghui Fan, Alperen Karaoglu, Jiaming Zhou
 *
 * This work is licensed under the MIT License. To view a copy of this
 * license, visit
 *
 *      https://opensource.org/licenses/MIT
 */

package org.ucl.fhirwork.mapping.executor.observation;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import org.junit.Before;
import org.junit.Test;
import org.ucl.fhirwork.common.framework.ExecutionException;
import org.ucl.fhirwork.configuration.ConfigService;
import org.ucl.fhirwork.configuration.data.ConfigType;
import org.ucl.fhirwork.configuration.data.GeneralConfig;
import org.ucl.fhirwork.mapping.data.ObservationFactory;
import org.ucl.fhirwork.mapping.query.QueryService;
import org.ucl.fhirwork.network.NetworkService;
import org.ucl.fhirwork.network.ehr.data.HealthRecord;
import org.ucl.fhirwork.network.ehr.data.ObservationBundle;
import org.ucl.fhirwork.network.ehr.exception.MissingHealthRecordException;
import org.ucl.fhirwork.network.ehr.server.EhrServer;
import org.ucl.fhirwork.network.empi.data.Identifier;
import org.ucl.fhirwork.network.empi.data.IdentifierDomain;
import org.ucl.fhirwork.network.empi.data.Person;
import org.ucl.fhirwork.network.empi.exception.IdentifierMissingException;
import org.ucl.fhirwork.network.empi.exception.MissingPersonException;
import org.ucl.fhirwork.network.empi.server.EmpiServer;
import org.ucl.fhirwork.network.fhir.operations.observation.ReadObservationOperation;
import org.ucl.fhirwork.network.fhir.operations.patient.UpdatePatientOperation;
import org.ucl.fhirwork.test.MockConfigService;
import org.ucl.fhirwork.test.MockNetworkService;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

public class ReadObservationExecutorTest
{
    private NetworkService networkService;
    private QueryService queryService;
    private ConfigService configService;
    private ObservationFactory observationFactory;
    private ReadObservationExecutor executor;

    @Before
    public void setup() throws Throwable
    {
        configService = MockConfigService.get();
        networkService = MockNetworkService.get();
        queryService = mock(QueryService.class);
        observationFactory = mock(ObservationFactory.class);
        executor = new ReadObservationExecutor(networkService, configService, queryService, observationFactory);
        setupMockBehaviour();
    }

    @Test
    public void setOperationTest()
    {
        TokenOrListParam codes = mock(TokenOrListParam.class);
        ReferenceParam patient = mock(ReferenceParam.class);

        ReadObservationOperation operation = new ReadObservationOperation(codes, patient);
        executor.setOperation(operation);
    }

    @Test (expected = NullPointerException.class)
    public void setOperationNullTest()
    {
        executor.setOperation(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void setIncorrectOperationTest()
    {
        IdDt patientId = mock(IdDt.class);
        Patient patient = mock(Patient.class);

        UpdatePatientOperation operation = new UpdatePatientOperation(patientId, patient);
        executor.setOperation(operation);
    }

    @Test
    public void invokeTest() throws Throwable
    {
        executor.setOperation(mockOperation());
        executor.invoke();

        EmpiServer empiServer = networkService.getEmpiServer();
        EhrServer ehrServer = networkService.getEhrServer();

        verify(empiServer, times(1)).loadPerson(anyString());
        verify(ehrServer, times(1)).getEhr(anyString(), anyString());
        verify(ehrServer, times(2)).query(anyString(),any());
    }

    @Test (expected = NullPointerException.class)
    public void invokeWithoutOperationTest() throws Throwable
    {
        try {
            executor.invoke();
        }
        catch (ExecutionException error){
            throw error.getCause();
        }
    }

    @Test (expected = MissingPersonException.class)
    public void invokePersonMissing() throws Throwable
    {
        try {
            EmpiServer empiServer = networkService.getEmpiServer();
            when(empiServer.loadPerson(anyString())).thenThrow(MissingPersonException.class);

            executor.setOperation(mockOperation());
            executor.invoke();
        }
        catch (ExecutionException error){
            throw error.getCause();
        }
    }

    @Test (expected = IdentifierMissingException.class)
    public void invokeIdentifierMissing() throws Throwable
    {
        try {
            GeneralConfig generalConfig = configService.getConfig(ConfigType.General);
            when(generalConfig.getEhrIdSystem()).thenReturn("http://different.com");

            executor.setOperation(mockOperation());
            executor.invoke();
        }
        catch (ExecutionException error){
            throw error.getCause();
        }
    }

    @Test (expected = MissingHealthRecordException.class)
    public void invokeHealthRecordMissing() throws Throwable
    {
        try {
            EhrServer ehrServer = networkService.getEhrServer();
            when(ehrServer.getEhr(anyString(), anyString())).thenThrow(new MissingHealthRecordException());

            executor.setOperation(mockOperation());
            executor.invoke();
        }
        catch (ExecutionException error){
            throw error.getCause();
        }
    }

    private void setupMockBehaviour() throws Throwable
    {
        GeneralConfig generalConfig = configService.getConfig(ConfigType.General);
        when(generalConfig.getEhrIdSystem()).thenReturn("http://fhirwork.com");

        EmpiServer empiServer = networkService.getEmpiServer();
        when(empiServer.loadPerson(anyString())).thenReturn(mockPerson());

        EhrServer ehrServer = networkService.getEhrServer();
        when(ehrServer.getEhr(anyString(), anyString())).thenReturn(mockHealthRecord());
        when(ehrServer.query(anyString(), any())).thenReturn(mockQueryBundle());

        when(queryService.isSupported(anyString())).thenReturn(true);
        when(queryService.getQuery(anyString(), anyString())).thenReturn("query");
        when(observationFactory.fromQueryBundle(anyString(), anyString(), any(ObservationBundle.class))).thenReturn(new ArrayList<>());
    }

    private ReadObservationOperation mockOperation()
    {
        TokenOrListParam codes = new TokenOrListParam("http://loinc.org", "3141-9", "8302-2");
        ReferenceParam patient = new ReferenceParam("123");
        return new ReadObservationOperation(codes, patient);
    }

    private Person mockPerson()
    {
        IdentifierDomain identifierDomain = new IdentifierDomain();
        identifierDomain.setIdentifierDomainName("http://fhirwork.com");

        Identifier identifier = new Identifier();
        identifier.setIdentifier("456");
        identifier.setIdentifierDomain(identifierDomain);
        Identifier[] identifiers = {identifier};

        Person person = new Person();
        person.setPersonId("123");
        person.setPersonIdentifiers(identifiers);
        return person;
    }

    private HealthRecord mockHealthRecord()
    {
        return new HealthRecord("789");
    }

    private ObservationBundle mockQueryBundle()
    {
        return new ObservationBundle();
    }
}

/*
 * FHIRWork (c) 2018 - Blair Butterworth, Abdul-Qadir Ali, Xialong Chen,
 * Chenghui Fan, Alperen Karaoglu, Jiaming Zhou
 *
 * This work is licensed under the MIT License. To view a copy of this
 * license, visit
 *
 *      https://opensource.org/licenses/MIT
 */

package org.ucl.fhirwork.mapping;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.ucl.fhirwork.common.framework.Executor;
import org.ucl.fhirwork.mapping.executor.*;
import org.ucl.fhirwork.network.fhir.operations.patient.DeletePatientOperation;

import javax.inject.Provider;

public class MappingServiceTest
{
    @Test
    public void getExecutorTest()
    {
        Provider<CreatePatientExecutor> createPatientProvider = createMockProvider(CreatePatientExecutor.class);
        Provider<DeletePatientExecutor> deletePatientProvider = createMockProvider(DeletePatientExecutor.class);
        Provider<ReadPatientExecutor> readPatientProvider = createMockProvider(ReadPatientExecutor.class);
        Provider<UpdatePatientExecutor> updatePatientProvider = createMockProvider(UpdatePatientExecutor.class);
        Provider<CreatePatientConditionalExecutor> createConditionalProvider = createMockProvider(CreatePatientConditionalExecutor.class);
        Provider<DeletePatientConditionalExecutor> deleteConditionalProvider = createMockProvider(DeletePatientConditionalExecutor.class);
        Provider<UpdatePatientConditionalExecutor> updateConditionalProvider = createMockProvider(UpdatePatientConditionalExecutor.class);

        MappingService mappingService = new MappingService(
                createPatientProvider, deletePatientProvider, readPatientProvider, updatePatientProvider,
                createConditionalProvider, deleteConditionalProvider, updateConditionalProvider);
        Executor executor = mappingService.getExecutor(new DeletePatientOperation(null));

        Assert.assertNotNull(executor);
        Mockito.verify(createPatientProvider, Mockito.never()).get();
        Mockito.verify(deletePatientProvider, Mockito.times(1)).get();
        Mockito.verify(readPatientProvider, Mockito.never()).get();
        Mockito.verify(updatePatientProvider, Mockito.never()).get();
    }

    @SuppressWarnings("unchecked")
    private <T> Provider<T> createMockProvider(Class<T> type)
    {
        T providedObject = Mockito.mock(type);
        Provider<T> provider = Mockito.mock(Provider.class);
        Mockito.when(provider.get()).thenReturn(providedObject);
        return provider;
    }
}

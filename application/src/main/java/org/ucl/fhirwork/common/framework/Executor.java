/*
 * FHIRWork (c) 2018 - Blair Butterworth, Abdul-Qadir Ali, Xialong Chen,
 * Chenghui Fan, Alperen Karaoglu, Jiaming Zhou
 *
 * This work is licensed under the MIT License. To view a copy of this
 * license, visit
 *
 *      https://opensource.org/licenses/MIT
 */

package org.ucl.fhirwork.common.framework;

/**
 * Implementors of this interface perform the actions required to execute an
 * operation.
 *
 * @author Blair Butterworth
 */
public interface Executor
{
    public void setOperation(Operation operation);

    public Object invoke() throws ExecutionException;
}

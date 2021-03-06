/*
 * FHIRWork (c) 2018 - Blair Butterworth, Abdul-Qadir Ali, Xialong Chen,
 * Chenghui Fan, Alperen Karaoglu, Jiaming Zhou
 *
 * This work is licensed under the MIT License. To view a copy of this
 * license, visit
 *
 *      https://opensource.org/licenses/MIT
 */

package org.ucl.fhirwork.network.ehr.server;

/**
 * Options in this enumeration specify different HTTP headers accepted by an
 * OpenEHR REST web service.
 *
 * @author Blair Butterworth
 */
public enum EhrHeader
{
    SessionId ("Ehr-Session");

    private String value;

    private EhrHeader(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

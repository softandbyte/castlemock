/*
 * Copyright 2015 Karl Dahlgren
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.castlemock.web.mock.rest.service.project;

import com.castlemock.core.basis.model.Service;
import com.castlemock.core.basis.model.ServiceResult;
import com.castlemock.core.basis.model.ServiceTask;
import com.castlemock.core.mock.rest.model.project.domain.RestMethod;
import com.castlemock.core.mock.rest.model.project.domain.RestMockResponse;
import com.castlemock.core.mock.rest.service.project.input.ReadRestMethodInput;
import com.castlemock.core.mock.rest.service.project.output.ReadRestMethodOutput;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author Karl Dahlgren
 * @since 1.0
 */
@org.springframework.stereotype.Service
public class ReadRestMethodService extends AbstractRestProjectService implements Service<ReadRestMethodInput, ReadRestMethodOutput> {

    private static final Logger LOGGER = Logger.getLogger(ReadRestMethodService.class);

    /**
     * The process message is responsible for processing an incoming serviceTask and generate
     * a response based on the incoming serviceTask input
     * @param serviceTask The serviceTask that will be processed by the service
     * @return A result based on the processed incoming serviceTask
     * @see ServiceTask
     * @see ServiceResult
     */
    @Override
    public ServiceResult<ReadRestMethodOutput> process(final ServiceTask<ReadRestMethodInput> serviceTask) {
        final ReadRestMethodInput input = serviceTask.getInput();
        final RestMethod restMethod = this.methodRepository.findOne(input.getRestMethodId());
        final List<RestMockResponse> mockResponses = this.mockResponseRepository.findWithMethodId(input.getRestMethodId());
        restMethod.setMockResponses(mockResponses);

        if(restMethod.getDefaultQueryMockResponseId() != null){
            // Iterate through all the mocked responses to identify
            // which has been set to be the default XPath mock response.
            boolean defaultQueryMockResponseId = false;
            for(RestMockResponse mockResponse : mockResponses){
                if(mockResponse.getId().equals(restMethod.getDefaultQueryMockResponseId())){
                    restMethod.setDefaultQueryResponseName(mockResponse.getName());
                    defaultQueryMockResponseId = true;
                    break;
                }
            }

            if(!defaultQueryMockResponseId){
                // Unable to find the default XPath mock response.
                // Log only an error message for now.
                LOGGER.error("Unable to find the default Query mock response with the following id: " +
                        restMethod.getDefaultQueryMockResponseId());
            }
        }

        return createServiceResult(ReadRestMethodOutput.builder()
                .restMethod(restMethod)
                .build());
    }
}

package com.castlemock.web.mock.rest.converter;

import com.castlemock.core.basis.model.http.domain.HttpMethod;
import com.castlemock.core.mock.rest.model.project.domain.RestMethodStatus;
import com.castlemock.core.mock.rest.model.project.domain.RestMockResponseStatus;
import com.castlemock.core.mock.rest.model.project.domain.RestResponseStrategy;
import com.castlemock.core.mock.rest.model.project.dto.RestApplicationDto;
import com.castlemock.core.mock.rest.model.project.dto.RestMethodDto;
import com.castlemock.core.mock.rest.model.project.dto.RestMockResponseDto;
import com.castlemock.core.mock.rest.model.project.dto.RestResourceDto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@link WADLRestDefinitionConverter} class provides functionality related to WADL.
 * @author Karl Dahlgren
 * @since 1.10
 */
class WADLRestDefinitionConverter extends AbstractRestDefinitionConverter {

    /**
     * The method is responsible for parsing a {@link File} and converting into a list of {@link RestApplicationDto}.
     * @param file The {@link File} be parsed and converted into a list of {@link RestApplicationDto}.
     * @param generateResponse Will generate a default response if true. No response will be generated if false.
     * @return A list of {@link RestApplicationDto} based on the provided file.
     */
    @Override
    public List<RestApplicationDto> convert(final File file, final boolean generateResponse){
        List<RestApplicationDto> applications = new LinkedList<RestApplicationDto>();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            document.getDocumentElement().normalize();

            List<Element> applicationElements = getApplications(document);

            for(Element applicationElement : applicationElements){
                final String applicationName = file.getName().replace(".wadl", "");
                final String baseUri = resourceBase(applicationElement);
                final RestApplicationDto restApplicationDto = new RestApplicationDto();
                restApplicationDto.setName(applicationName);
                applications.add(restApplicationDto);

                final List<Element> resourceElements = getResources(applicationElement);
                for(Element resourceElement : resourceElements){
                    final String resourceName = resourceElement.getAttribute("path");
                    final RestResourceDto restResourceDto = new RestResourceDto();
                    restResourceDto.setName(resourceName);
                    restResourceDto.setUri(baseUri + resourceName);
                    restApplicationDto.getResources().add(restResourceDto);

                    final List<Element> methodElements = getMethods(resourceElement);
                    for(Element methodElement : methodElements){
                        final String methodName = methodElement.getAttribute("id");
                        final String methodType = methodElement.getAttribute("name");

                        final RestMethodDto restMethodDto = new RestMethodDto();
                        restMethodDto.setName(methodName);
                        restMethodDto.setHttpMethod(HttpMethod.valueOf(methodType));
                        restMethodDto.setStatus(RestMethodStatus.MOCKED);
                        restMethodDto.setResponseStrategy(RestResponseStrategy.RANDOM);
                        restMethodDto.setMockResponses(new ArrayList<RestMockResponseDto>());


                        if(generateResponse){
                            RestMockResponseDto restMockResponse = generateResponse();
                            restMethodDto.getMockResponses().add(restMockResponse);
                        }

                        restResourceDto.getMethods().add(restMethodDto);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable parse WADL file", e);
        }

        return applications;
    }

    /**
     * The method extracts all the application elements from the provided document
     * @param document The document which contains all the application that will be extracted
     * @return A list of application elements
     */
    private List<Element> getApplications(Document document){
        List<Element> applicationElements = new LinkedList<Element>();
        final NodeList applicationNodeList = document.getElementsByTagName("application");

        for (int applicationIndex = 0; applicationIndex < applicationNodeList.getLength(); applicationIndex++) {
            Node applicationNode = applicationNodeList.item(applicationIndex);
            if (applicationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element applicationElement = (Element) applicationNode;
                applicationElements.add(applicationElement);
            }
        }
        return applicationElements;
    }

    /**
     * The method extracts all the resource elements from the provided application element
     * @param applicationElement The application element which contains all the resources that will be extracted
     * @return A list of resource elements
     */
    private List<Element> getResources(Element applicationElement){
        List<Element> resourceElements = new LinkedList<Element>();
        NodeList resourceNodeList = applicationElement.getElementsByTagName("resource");

        for (int resourceIndex = 0; resourceIndex < resourceNodeList.getLength(); resourceIndex++) {
            Node resourceNode = resourceNodeList.item(resourceIndex);
            if (resourceNode.getNodeType() == Node.ELEMENT_NODE) {
                Element resourceElement = (Element) resourceNode;
                resourceElements.add(resourceElement);
            }
        }
        return resourceElements;
    }

    /**
     * The method extracts all the method elements from the provided resource element
     * @param resourceElement The resource element which contains all the methods that will be extracted
     * @return A list of method elements
     */
    private List<Element> getMethods(Element resourceElement){
        List<Element> methodElements = new LinkedList<Element>();
        NodeList methodNodeList = resourceElement.getElementsByTagName("method");

        for (int methodIndex = 0; methodIndex < methodNodeList.getLength(); methodIndex++) {
            Node methodNode = methodNodeList.item(methodIndex);
            if (methodNode.getNodeType() == Node.ELEMENT_NODE) {
                Element methodElement = (Element) methodNode;
                methodElements.add(methodElement);
            }
        }
        return methodElements;
    }

    /**
     * The method provides the functionality to extract the resource base from a provided application element
     * @param applicationElement The application element that contains the resource base
     * @return The resource base from the application element
     * @throws MalformedURLException
     */
    private String resourceBase(Element applicationElement) throws MalformedURLException {
        final NodeList resourcesNodeList = applicationElement.getElementsByTagName("resources");
        for (int resourcesIndex = 0; resourcesIndex < resourcesNodeList.getLength(); resourcesIndex++) {
            Node resourcesNode = resourcesNodeList.item(resourcesIndex);
            if (resourcesNode.getNodeType() == Node.ELEMENT_NODE) {
                Element resourcesElement = (Element) resourcesNode;
                String resourceBase = resourcesElement.getAttribute("base");
                URL url = new URL(resourceBase);
                return url.getPath();
            }
        }
        return null;
    }

}

package com.hp.octane.integrations.dto;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;

/**
 * Created by gullery on 08/02/2016.
 * <p/>
 * API definition of an internal DTO factories
 */

public abstract class DTOInternalProviderBase {

	protected DTOInternalProviderBase() {
	}

	protected abstract Class[] getXMLAbleClasses();

	protected abstract <T> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException;

	<T extends DTOBase> String toXML(T dto) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(getXMLAbleClasses());
		Marshaller marshaller = jaxbContext.createMarshaller();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.marshal(dto, baos);
		return baos.toString();
	}

	<T extends DTOBase> T fromXml(String xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(getXMLAbleClasses());
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return (T) unmarshaller.unmarshal(new StringReader(xml));
	}

	<T extends DTOBase> T fromXmlFile(File xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(getXMLAbleClasses());
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return (T) unmarshaller.unmarshal(xml);
	}
}

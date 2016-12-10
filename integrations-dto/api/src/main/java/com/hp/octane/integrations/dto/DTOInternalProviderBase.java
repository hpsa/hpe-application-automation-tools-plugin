package com.hp.octane.integrations.dto;

import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.util.Set;

/**
 * Created by gullery on 08/02/2016.
 * <p/>
 * API definition of an internal DTO factories
 */

public abstract class DTOInternalProviderBase {

	protected DTOInternalProviderBase() {
	}

	protected abstract void provideImplResolvingMap(SimpleAbstractTypeResolver dtoImplResolver);

	protected abstract Set<Class<? extends DTOBase>> getJSONAbleDTOs();

	protected Class[] getXMLAbleDTOs() {
		return new Class[0];
	}

	protected abstract <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException;

	<T extends DTOBase> String toXML(T dto) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(getXMLAbleDTOs());
		Marshaller marshaller = jaxbContext.createMarshaller();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.marshal(dto, baos);
		return baos.toString();
	}

	<T extends DTOBase> T fromXml(String xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(getXMLAbleDTOs());
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return (T) unmarshaller.unmarshal(new StringReader(xml));
	}

	<T extends DTOBase> T fromXmlFile(File xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(getXMLAbleDTOs());
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return (T) unmarshaller.unmarshal(xml);
	}
}

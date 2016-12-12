package com.hp.octane.integrations.dto;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by gullery on 08/02/2016.
 * <p/>
 * API definition of an internal DTO factories
 */

public abstract class DTOInternalProviderBase {
	protected final Map<Class<? extends DTOBase>, Class> dtoPairs = new LinkedHashMap<>();
	protected final List<Class<? extends DTOBase>> xmlAbles = new LinkedList<>();

	protected DTOInternalProviderBase() {
	}

	protected abstract <T extends DTOBase> T instantiateDTO(Class<T> targetType) throws InstantiationException, IllegalAccessException;

	<T extends DTOBase> String toXML(T dto) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(this.getXMLAbles());
		Marshaller marshaller = jaxbContext.createMarshaller();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.marshal(dto, baos);
		return baos.toString();
	}

	<T extends DTOBase> T fromXml(String xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(getXMLAbles());
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return (T) unmarshaller.unmarshal(new StringReader(xml));
	}

	<T extends DTOBase> T fromXmlFile(File xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(getXMLAbles());
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return (T) unmarshaller.unmarshal(xml);
	}

	Map<Class<? extends DTOBase>, Class> getDTOPairs() {
		return dtoPairs;
	}

	private Class[] getXMLAbles() {
		return xmlAbles.toArray(new Class[xmlAbles.size()]);
	}
}

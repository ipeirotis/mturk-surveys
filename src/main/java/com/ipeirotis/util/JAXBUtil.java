package com.ipeirotis.util;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JAXBUtil {

    public static String marshal(Object object) {
        try {
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(object, writer);

            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("Error marshalling object", e);
        }
    }

    public static Object unmarshal(InputStream is, Class<?> clazz) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Object object = unmarshaller.unmarshal(is);

            return object;
        } catch (JAXBException e) {
            throw new RuntimeException("Error unmarshalling object", e);
        }
    }
}

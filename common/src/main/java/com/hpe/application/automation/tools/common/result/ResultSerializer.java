package com.hpe.application.automation.tools.common.result;

import com.hpe.application.automation.tools.common.SSEException;
import com.hpe.application.automation.tools.common.result.model.junit.Testsuites;
import com.hpe.application.automation.tools.common.sdk.Logger;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dsinelnikov on 7/31/2015.
 * Static class
 */
public final class ResultSerializer {

    private ResultSerializer(){}

    public static String saveResults(Testsuites testsuites, String workingDirectory, Logger logger)
            throws SSEException
    {
        String filePath = getFullFilePath(workingDirectory, getFileName());

        try
        {
            if (testsuites != null)
            {
                StringWriter writer = new StringWriter();
                JAXBContext context = JAXBContext.newInstance(Testsuites.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(testsuites, writer);

                PrintWriter resultWriter = new PrintWriter(filePath);
                resultWriter.print(writer.toString());
                resultWriter.close();

                return filePath;
            }
            else
            {
                String message ="Empty Results";
                logger.log(message);
                throw new SSEException(message);
            }
        }
        catch (Throwable cause)
        {
            String message=String.format(
                    "Failed to create run results, Exception: %s",
                    cause.getMessage());
            logger.log(message);

            throw new SSEException(message);
        }
    }

    public static Testsuites Deserialize(File file) throws JAXBException
    {
        JAXBContext jaxbContext = JAXBContext.newInstance(Testsuites.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Testsuites testsuites = (Testsuites)jaxbUnmarshaller.unmarshal(file);

        return testsuites;
    }

    private static String getFullFilePath(String workingDirectoryPath, String fileName)
    {
        return Paths.get(workingDirectoryPath, fileName).toString();
    }

    private static String getFileName() {

        Format formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
        String time = formatter.format(new Date());
        return String.format("Results%s.xml", time);
    }
}

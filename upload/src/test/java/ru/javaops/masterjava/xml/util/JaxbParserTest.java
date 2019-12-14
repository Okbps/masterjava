package ru.javaops.masterjava.xml.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;
import ru.javaops.masterjava.xml.schema.CityType;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.util.concurrent.*;

@SuppressWarnings("UnstableApiUsage")
public class JaxbParserTest {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    @Test
    public void testPayload() throws Exception {
//        JaxbParserTest.class.getResourceAsStream("/city.xml")
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());
        String strPayload = JAXB_PARSER.marshal(payload);
        JAXB_PARSER.validate(strPayload);
        System.out.println(strPayload);
    }

    @Test
    public void testCity() throws Exception {
        JAXBElement<CityType> cityElement = JAXB_PARSER.unmarshal(
                Resources.getResource("city.xml").openStream());
        CityType city = cityElement.getValue();
        JAXBElement<CityType> cityElement2 =
                new JAXBElement<>(new QName("http://javaops.ru", "City"), CityType.class, city);
        String strCity = JAXB_PARSER.marshal(cityElement2);
        JAXB_PARSER.validate(strCity);
        System.out.println(strCity);
    }

    @Test
    public void testPayloadMulti() throws Exception {
        final int iterations = 100;
        String[] fileNames = {"payload.xml", "payload2.xml"};
        String[] originals = new String[2];
        originals[0] = Resources.toString(Resources.getResource(fileNames[0]), Charsets.UTF_8);
        originals[1] = Resources.toString(Resources.getResource(fileNames[1]), Charsets.UTF_8);

        JaxbParser jaxbMulti = new JaxbParser();
        jaxbMulti.setSchema(Schemas.ofClasspath("payload.xsd"));

        class PayloadResult{
            private final String original;
            Payload payload1, payload2;

            public PayloadResult(String original) {
                this.original = original;
            }
        }

        ExecutorService exec = Executors.newFixedThreadPool(2);
        CompletionService<PayloadResult> service = new ExecutorCompletionService<>(exec);

        for (int i = 0; i < iterations; i++) {
            int j = i % 2;
            service.submit(()->{
                PayloadResult result = new PayloadResult(originals[j]);
                try {
                    result.payload1 = jaxbMulti.unmarshal(result.original);
                    String strPayload = jaxbMulti.marshal(result.payload1);
                    result.payload2 = jaxbMulti.unmarshal(strPayload);
                } catch (JAXBException e) {
                    e.printStackTrace();
                }

                return  result;
            });
        }

        for (int i = 0; i < iterations; i++) {
            Future<PayloadResult> future = service.take();
            Assert.assertEquals(future.get().payload1, future.get().payload2);
        }
    }

//    @Test
//    public void testCityMulti() throws Exception {
//
//    }
}
package io.jsonwebtoken.impl

import io.jsonwebtoken.Clock
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import org.junit.Ignore
import org.junit.Test

import java.text.DateFormat
import java.text.SimpleDateFormat

import static org.junit.Assert.*

/**
 * Created by GSP on 2/14/18.
 */
class DefaultJwtParserTest {



    @Test
    @Ignore
    void myTest() {
        DefaultJwtParser parser = new DefaultJwtParser();

        String jwt = "eyJraWQiOiJ0ZzFzIiwiYWxnIjoiUlMyNTYifQ." +
                "eyJzdWIiOiIwMDA1YWE0MC1kYzZmLTExZTUtYjU4ZC04MTMxMGNlNzdiZjkiLCJpc3MiOiJNSTYiLCJleHAiOjE1MTkwNTk0NDcsImlhdCI6MTUxOTA1NTg0NywianRpIjoiNjQ3MDgzMGUtNGE5Yy00Yzk1LWJiM2EtNzE3OTk0ZGIxZmM2Iiwic2t5IjoidGcxcyIsInN1dCI6IlIiLCJjbGkiOiJtaTYtdGVzdC0xLjAuMCIsInNjbyI6ImVjb20ubWVkLG9wZW5pZCIsImVpZCI6ImVnczFAdGFyZ2V0LmNvbSIsImFzbCI6Ik0ifQ."


        try {
            Jwt responseJwt = parser.parse(jwt);
            assertNotNull(responseJwt);
        } catch (Exception e) {
            fail("Exception was thrown: " + e.message)
        }
    }

    @Test
    @Ignore
    void myTest2() {
        DefaultJwtParser parser = new DefaultJwtParser();
        String jwt = "{" +
                "  \"exp\": 1518807307" +
                "}"

        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setPayload(jwt)

        try {
            Jwt responseJwt = parser.parse(jwtBuilder.compact());
            assertNotNull(responseJwt);
        } catch (Exception e) {
            fail("Exception was thrown: " + e.message)
        }
    }


    @Test
    void testDST() {
        String string = "03/12/2017 1:59:59";
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        Date date = df.parse(string);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        System.out.println(df.format(cal.getTime()));

        cal.add(Calendar.SECOND, 1);

        System.out.println(df.format(cal.getTime()));
    }

    @Test
    void testDSTOff() {
        String string = "11/05/2017 1:59:59";
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        Date date = df.parse(string);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        System.out.println(df.format(cal.getTime()));

        cal.add(Calendar.SECOND, 1);

        System.out.println(df.format(cal.getTime()));
    }


    @Test
    void testExpirationDateWithJWTNotExpired() {

        DefaultJwtParser parser = new DefaultJwtParser();

        Calendar exp = Calendar.getInstance()
        exp.add(Calendar.MINUTE, 30);

        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setExpiration(exp.getTime())

        try {
            Jwt responseJwt = parser.parse(jwtBuilder.compact());
            assertNotNull(responseJwt);
        } catch (Exception e) {
            fail("Exception was thrown: " + e.message)
        }
    }

    @Test
    void testExpirationDateWithJWTExpired() {

        DefaultJwtParser parser = new DefaultJwtParser();

        Calendar exp = Calendar.getInstance()
        exp.add(Calendar.MINUTE, -30);

        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setExpiration(exp.getTime())

        try {
            Jwt responseJwt = parser.parse(jwtBuilder.compact());
            fail("ExpiredJwtException expected")
        } catch (ExpiredJwtException e) {
            assertTrue(e.getClaims().containsKey("exp"))
        } catch (Exception e) {
            fail("Exception was thrown: " + e.message)
        }
    }

    @Test
    // This is in the Spring
    void testExpirationDateWithJWTDuringDSTOnSwitch() {

        DefaultJwtParser parser = new DefaultJwtParser();
        Clock clock = new DSTOnClock()
        parser.setClock(clock)

        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setExpiration(new Date(clock.now().getTime() + 30 * 60 * 1000))   // 30 minutes into the future
        clock.incrementSeconds();

        try {
            Jwt responseJwt = parser.parse(jwtBuilder.compact());
            assertNotNull(responseJwt);
        } catch (ExpiredJwtException e) {
            fail("DST had token expired when it should still be active")
        } catch (Exception e) {
            fail("Exception was thrown: " + e.message)
        }
    }

    //This is turning DST on in the Spring where the clock goes forward after dates in a token are set
    public class DSTOnClock implements Clock {

        Calendar cal = Calendar.getInstance()

        public DSTOnClock() {
            String string = "03/09/2014 1:59:59";
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = df.parse(string);

            cal.setTime(date);
        }

        @Override
        Date now() {
            return cal.getTime();
        }

        void incrementSeconds() {
            cal.add(Calendar.SECOND, 1)
        }
    }

    @Test
    // This is in the Fall
    void testExpirationDateWithJWTDuringDSTOffSwitch() {

        DefaultJwtParser parser = new DefaultJwtParser();
        parser.setClock(new DSTOffClock())

        Calendar exp = Calendar.getInstance()
        exp.add(Calendar.MINUTE, 30);

        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setExpiration(exp.getTime())

        try {
            Jwt responseJwt = parser.parse(jwtBuilder.compact());
            assertNotNull(responseJwt);
        } catch (ExpiredJwtException e) {
            fail("DST had token expired when it should still be active")
        } catch (Exception e) {
            fail("Exception was thrown: " + e.message)
        }
    }

    //This is turning DST off in the Fall where the clock goes backwards after dates in a token are set
    public class DSTOffClock implements Clock {

        @Override
        Date now() {
            Calendar cal = Calendar.getInstance()
            cal.add(Calendar.HOUR, -1)
            return cal.getTime();
        }
    }
}

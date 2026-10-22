package com.helger.nss.supplementary.security;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Security;
import java.time.Duration;

import javax.net.ssl.SSLException;

/**
 * Checks whether the JVM accepts a TLS chain that contains a compromised intermediate CA. If the
 * connection succeeds, the problem was not detected. Usage:
 * <p>
 * java MainCheckeMahudraCAs.java (JVM defaults)
 * <p>
 * java MainCheckeMahudraCAs.java -r (additionally enable OCSP/CRL revocation checks)
 */
public class MainCheckeMahudraCAs
{
  private static final String TEST_URLS[] = { "https://dv-g3b-emudhra.we-have-a-problem.com/",
                                              "https://dv-g3c-emudhra.we-have-a-problem.com/",
                                              "https://ov-g3b-emudhra.we-have-a-problem.com/",
                                              "https://ov-g3c-emudhra.we-have-a-problem.com/",
                                              "https://ev-g3b-emudhra.we-have-a-problem.com/",
                                              "https://ev-g3c-emudhra.we-have-a-problem.com/",
                                              "https://dv-g3b-aaa-emudhra.we-have-a-problem.com/",
                                              "https://dv-g3c-aaa-emudhra.we-have-a-problem.com/",
                                              "https://ov-g3b-aaa-emudhra.we-have-a-problem.com/",
                                              "https://ov-g3c-aaa-emudhra.we-have-a-problem.com/",
                                              "https://ev-g3b-aaa-emudhra.we-have-a-problem.com/",
                                              "https://ev-g3c-aaa-emudhra.we-have-a-problem.com/" };

  public static void main (final String [] args) throws InterruptedException
  {
    if (true || args.length > 0 && args[0].equals ("-r"))
    {
      // Must be set before the first TLS context is created
      Security.setProperty ("ocsp.enable", "true");
      System.setProperty ("com.sun.net.ssl.checkRevocation", "true");
      System.setProperty ("com.sun.security.enableCRLDP", "true");
    }

    final HttpClient client = HttpClient.newBuilder ().connectTimeout (Duration.ofSeconds (10)).build ();

    for (final String url : TEST_URLS)
    {
      _verifyTLS (client, url);
    }
  }

  private static void _verifyTLS (final HttpClient client, final String url) throws InterruptedException
  {
    System.out.println ("Trying to connect to '" + url + "'");
    final HttpRequest request = HttpRequest.newBuilder (URI.create (url))
                                           .timeout (Duration.ofSeconds (15))
                                           .GET ()
                                           .build ();

    try
    {
      final HttpResponse <Void> response = client.send (request, HttpResponse.BodyHandlers.discarding ());
      // Handshake succeeded -> certificate chain was accepted
      System.err.println ("VULNERABLE (HTTP " + response.statusCode () + ") " + url);
    }
    catch (final IOException e)
    {
      final SSLException ssl = _findSslException (e);
      if (ssl != null)
      {
        // Handshake was rejected -> problem detected
        System.out.println ("OK " + url);
        System.out.println ("  " + ssl.getClass ().getSimpleName () + ": " + ssl.getMessage ());
        return;
      }
      // DNS, timeout, connection refused etc. -> says nothing about TLS
      System.err.println ("ERROR: " + e.getClass ().getSimpleName () + ": " + e.getMessage ());
    }
  }

  // The HTTP client may wrap the SSL exception, so walk the cause chain
  private static SSLException _findSslException (final Throwable tsrc)
  {
    Throwable t = tsrc;
    while (t != null)
    {
      if (t instanceof final SSLException sslex)
        return sslex;
      t = t.getCause ();
    }
    return null;
  }
}

package ru.litonchik.edt.themes.model;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.function.BooleanSupplier;

/** Finite, cancellable HTTP read; must be called on a background worker. */
public final class ReleaseFeedClient implements ReleaseSource {
    public static final URI FEED=URI.create("https://litonchik.github.io/ones_edt_themes/repository/release.json");
    @FunctionalInterface public interface Connections {HttpURLConnection open(URI uri) throws IOException;}
    private final URI uri;private final Connections connections;private final int timeout;
    public ReleaseFeedClient(URI uri,Connections connections,int timeout){
        if(uri==null || connections==null || timeout<1 || timeout>10000 || uri.getUserInfo()!=null || uri.getFragment()!=null || !("https".equals(uri.getScheme()) || ("http".equals(uri.getScheme()) && ("127.0.0.1".equals(uri.getHost()) || "[::1]".equals(uri.getHost())))))throw new IllegalArgumentException("Некорректный адрес проверки");
        this.uri=uri;this.connections=connections;this.timeout=timeout;
    }
    @Override public ReleaseInfo read(BooleanSupplier cancelled) throws IOException {
        if(cancelled.getAsBoolean())throw new InterruptedIOException("Проверка отменена");
        var connection=connections.open(uri);
        try{
            connection.setConnectTimeout(timeout);connection.setReadTimeout(timeout);connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept","application/json");connection.setRequestProperty("User-Agent","EDT-Themes-Update-Check");
            if(connection.getResponseCode()!=200)throw new IOException("HTTP release feed unavailable");
            if(cancelled.getAsBoolean())throw new InterruptedIOException("Проверка отменена");
            if(connection.getContentLengthLong()>8192)throw new IOException("Release feed too large");
            long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(timeout*2L);
            try(var input=connection.getInputStream();var output=new ByteArrayOutputStream()){
                byte[] buffer=new byte[1024];int count;
                while((count=input.read(buffer))!=-1){
                    if(cancelled.getAsBoolean())throw new InterruptedIOException("Проверка отменена");
                    if(System.nanoTime()>deadline)throw new java.net.SocketTimeoutException("Release feed deadline");
                    if(output.size()+count>8192)throw new IOException("Release feed too large");output.write(buffer,0,count);
                }
                try{
                    var decoder=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
                    return ReleaseInfo.parse(decoder.decode(java.nio.ByteBuffer.wrap(output.toByteArray())).toString());
                }catch(IllegalArgumentException e){throw new IOException("Invalid release feed",e);}
            }
        }finally{connection.disconnect();}
    }
}

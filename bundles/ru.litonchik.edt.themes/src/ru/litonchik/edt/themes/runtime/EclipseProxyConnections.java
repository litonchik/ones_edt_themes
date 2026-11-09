package ru.litonchik.edt.themes.runtime;

import java.io.IOException;
import java.net.*;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.preferences.*;

/** Noninteractive network selection: never unlock secure storage or invoke the global authenticator. */
public final class EclipseProxyConnections {
    private static final String NODE="org.eclipse.core.net";
    private EclipseProxyConnections(){}
    private static String setting(String child,String key,String fallback){
        for(IScopeContext scope:new IScopeContext[]{ConfigurationScope.INSTANCE,InstanceScope.INSTANCE}){
            org.osgi.service.prefs.Preferences node=scope.getNode(NODE);if(!child.isEmpty())node=node.node(child);String value=node.get(key,null);if(value!=null)return value;
        }return fallback;
    }
    public static HttpURLConnection open(URI uri) throws IOException {
        String type="https".equals(uri.getScheme())?"HTTPS":"HTTP",host=null,user=null,password=null;int port=-1;boolean socks=false;
        boolean enabled=Boolean.parseBoolean(setting("","proxiesEnabled","true"));
        String excluded=setting("","nonProxiedHosts",System.getProperty("http.nonProxyHosts",""));
        boolean bypass=false;for(String pattern:excluded.split("[|,;]"))if(!pattern.isBlank() && uri.getHost().matches("(?i)"+Pattern.quote(pattern.trim()).replace("*","\\E.*\\Q"))){bypass=true;break;}
        if(enabled && !bypass){
            boolean system=Boolean.parseBoolean(setting("","systemProxiesEnabled","false"));
            if(!system){host=setting("proxyData/"+type,"host",null);try{port=Integer.parseInt(setting("proxyData/"+type,"port","-1"));}catch(NumberFormatException ignored){}
                if((host==null || host.isBlank())){host=setting("proxyData/SOCKS","host",null);try{port=Integer.parseInt(setting("proxyData/SOCKS","port","-1"));}catch(NumberFormatException ignored){}socks=host!=null && !host.isBlank();}
            }
            // Eclipse also exposes already-resolved proxy settings/credentials as JVM properties.
            // Read those in memory; do not query IProxyService.select(), which may unlock secure storage.
            String prefix=type.toLowerCase(java.util.Locale.ROOT)+".proxy";
            if(host==null || host.isBlank()){host=System.getProperty(prefix+"Host");try{port=Integer.parseInt(System.getProperty(prefix+"Port","-1"));}catch(NumberFormatException ignored){}}
            if(host!=null && host.equalsIgnoreCase(System.getProperty(prefix+"Host",""))){user=System.getProperty(prefix+"User");password=System.getProperty(prefix+"Password");}
        }
        if(host!=null && !host.isBlank() && (port<1 || port>65535))throw new IOException("Некорректный прокси EDT");
        if(socks)throw new IOException("SOCKS не используется для неинтерактивной проверки обновлений");
        Proxy proxy=host==null || host.isBlank()?Proxy.NO_PROXY:new Proxy(socks?Proxy.Type.SOCKS:Proxy.Type.HTTP,new InetSocketAddress(host,port));
        var connection=(HttpURLConnection)uri.toURL().openConnection(proxy);
        String proxyHost=host,proxyUser=user,proxyPassword=password;int proxyPort=port;
        // An origin/proxy challenge must never fall back to Eclipse's interactive global authenticator.
        connection.setAuthenticator(new Authenticator(){@Override protected PasswordAuthentication getPasswordAuthentication(){
            return getRequestorType()==RequestorType.PROXY && proxyHost!=null && proxyUser!=null && proxyPassword!=null && proxyHost.equalsIgnoreCase(getRequestingHost()) && proxyPort==getRequestingPort()?new PasswordAuthentication(proxyUser,proxyPassword.toCharArray()):null;
        }});
        return connection;
    }
}

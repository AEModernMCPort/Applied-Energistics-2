package appeng.core.lib.module;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * Created by Johannes on 13.09.2016.
 */
public class ModuleLoaderHelper {
    /**
     * Loads modules if and only if they have loadable dependencies.
     * Also orders them topologically.
     */
    public static List<String> checkAndOrderModules(Map<String, Pair<Class<Module>, String>> foundModules, Side currentSide, Map<String, Class<Module>> result){
        List<String> order = Lists.newArrayList();
        List<String> checked = Lists.newArrayList();
        for( String key: foundModules.keySet() )
        {
            if(!checked.contains(key)){
                validateAndPutModule(key, foundModules, currentSide, result, order, checked);
            }
        }
        return order;
    }

    //Basic toposorting, modules on which modules depend are put in front of modules depending on them
    private static void validateAndPutModule(String key, Map<String, Pair<Class<Module>, String>> foundModules, Side currentSide, Map<String, Class<Module>> result, List<String> order, List<String> checked){
        checked.add(key);
        boolean load = true;
        Pair<Class<Module>, String> entry = foundModules.get(key);
        if(entry.getRight() != null )
        {
            for( String dep : entry.getRight().split( ";" ) )
            {
                String[] depkv = dep.split( ":" );
                String[] keys = depkv[0].split( "\\-" );
                String value = depkv.length > 0 ? depkv[1] : null;

                Side side = ArrayUtils.contains( keys, "client" ) ? Side.CLIENT : ArrayUtils.contains( keys, "server" ) ? Side.SERVER : null;
                boolean required = ArrayUtils.contains( keys, "required" );
                boolean force = ArrayUtils.contains( keys, "force" );

                if( side != null && value == null ) //Side-only generous modules
                {
                    load &= side == currentSide;
                    if( side == currentSide || !force )
                    {
                        continue;
                    }
                }
                else if( required && value != null ) //look for mod
                {
                    String what = value.substring( 0, value.indexOf( '-' ) );
                    String which = value.substring( value.indexOf( '-' ) + 1, value.length() );
                    boolean found = side == null || side == currentSide;
                    switch( what )
                    {
                        case "mod":
                            found &= Loader.isModLoaded( which );
                            break;
                        case "module":
                            if(order.contains(which))
                                break;
                            if(checked.contains(which)) {//order does not, but checked does
                                found = false;
                                break;
                            }
                            if(foundModules.containsKey(which)){
                                validateAndPutModule(which, foundModules, currentSide, result, order, checked);
                                found &= order.contains(which);//validateAndPutModule puts it there
                            }else found = false;
                            break;
                        default:
                            found = false;
                    }
                    if( found || !force )
                    {
                        load &= found;
                        continue;
                    }
                }
                //TODO 1.10.2-MODUSEP - Report this in a fancier way ;)... Maybe >D...
                throw new RuntimeException( String.format( "Missing hard required dependency for module %s - %s", key, dep ) );
            }
        }
        if( load )
        {
            result.put(key, entry.getLeft() );
            order.add(key);
        }
    }
}

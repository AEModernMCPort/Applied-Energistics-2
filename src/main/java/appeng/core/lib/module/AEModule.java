
package appeng.core.lib.module;


import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Annotate {@link Module} with this annotation and make sure it has no-args constructor to add it to AE.
 * 
 * @author elix_x
 *
 */
@Retention( RUNTIME )
@Target( TYPE )
public @interface AEModule
{
	/**
	 * 
	 * @return Name of the module
	 */
	String value();

	/**
	 * Use this to make your addon dependent of something using elix-x fancy dependency syntax:
	 * Each module dependency statement is seperated by a semicolon (;).
	 * To declare your module side-only, just use "client" or "server" as one dependency statement
	 * Use "crash" if you want to stop game loading when used on the wrong side
	 * You can also declare dependency on a mod or AE2 module.
	 * For that, write your modifiers, seperated by a dash (-), followed by a colon (:), and the thing you want to depend on
	 * modifiers are: 	"server"/"client" if you only depend on it on either client or server
	 * 					"hard" will make that dependency hard, aka your module will load if and only if it is there
	 * 					"soft" will just put your module in front of that module.
	 * 					You have to use either "hard" or "soft"
	 * 					"before" will make you module load before the module specified here, aka it reverses the dependency
	 * 					"crash" will make the game crash if the dependency is not there instead of just not loading your module.
	 * 						Use with "hard"
	 * You can depend on either a mod or a AE2 module.
	 * For mods use "mod-<modid>", repacing <modid> with the modid of the mod you want to depend on.
	 * For modules use "module-<name>", replacing <name> with the name of the module you want to depend on.
	 * *If you are not making an internal module the order here will have little to no effect onto the order your module will be initialized*
	 * *because Forge calls your pre-/post-/Init methods. Therefore you have to specify the dependency structure in @Mod. Also make sure to depend on AE*
	 * You can also leave it empty or choose to ignore it if you do not have dependencies.
	 * @return module's dependencies
	 */
	String dependencies() default "";

}

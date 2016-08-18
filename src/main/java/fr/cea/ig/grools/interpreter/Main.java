package fr.cea.ig.grools.interpreter;
/*
 * Copyright LABGeM 07/07/16
 *
 * author: Jonathan MERCIER
 *
 * This software is a computer program whose purpose is to annotate a complete genome.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */


import ch.qos.logback.classic.Logger;
import fr.cea.ig.grools.Verbosity;
import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.settings.Settings;
import org.slf4j.LoggerFactory;

import fr.cea.ig.grools.Mode;
import fr.cea.ig.grools.Reasoner;
import fr.cea.ig.grools.reasoner.ConceptGraph;
import fr.cea.ig.grools.reasoner.ReasonerImpl;
import lombok.NonNull;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.settings.SettingsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
/*
 * @startuml
 * class Main{
 * }
 * @enduml
 */
public class Main {
    private final static Logger LOGGER = ( Logger ) LoggerFactory.getLogger( Main.class );
    private final static String APPNAME = "grools-drools-interpreter";

    private static Reasoner load( @NonNull final File file ) {
        Reasoner reasoner = null;
        final FileInputStream fis;
        try {
            fis = new FileInputStream( file );
            final ObjectInputStream ois = new ObjectInputStream( fis );
            final boolean hasBeenProceesed = ois.readBoolean( );
            final Mode mode = ( Mode ) ois.readObject( );
            final Verbosity verbosity = ( Verbosity ) ois.readObject( );
            final ConceptGraph graph = ( ConceptGraph ) ois.readObject( );
            reasoner = new ReasonerImpl( graph, mode, verbosity, hasBeenProceesed );
        }
        catch ( ClassNotFoundException e ) {
            LOGGER.error( e.getMessage( ) );
            System.exit( 1 );
        }
        catch ( FileNotFoundException e ) {
            LOGGER.error( "File: " + file.toString( ) + "not found" );
            System.exit( 1 );
        }
        catch ( IOException e ) {
            LOGGER.error( "Can not read/write into " + file.toString( ) );
            System.exit( 1 );
        }
        return reasoner;
    }

    private static void showHelp( ) {
        System.out.println( APPNAME + " grools_file" );
        System.out.println( APPNAME + " -h        display this help" );
    }

    public static void main( String[] args ) {
        //args = new String[]{"/media/sf_agc/proj/Grools/res/UP000000430-AbaylyiADP1/GenomeProperties/reasoner.grools"};
        //args = new String[]{"/media/sf_agc/proj/Grools/res/UP000000430-AbaylyiADP1/18082016-Unipathway/reasoner.grools"};
        if ( args.length != 1 ) {
            System.err.println( "Error " + APPNAME + " needs a grools file" );
            showHelp( );
            System.exit( 1 );
        }
        else if ( args[ 0 ].equals( "-h" ) ) {
            showHelp( );
            System.exit( 0 );
        }

        final Reasoner reasoner = load( new File( args[ 0 ] ) );

        System.out.println( "=== Query shell interpreter ===" );
        System.out.println( "Enter quit to leave" );
        final Settings settings = new SettingsBuilder( ).logging( false )
                                                        .enableMan( false )
                                                        .readInputrc( false )
                                                        .create( );
        final Prompt prompt = new Prompt( "> " );
        final Console console = new Console( settings );
        console.setPrompt( prompt );
        console.setCompletionEnabled( true );
        console.addCompletion( new GroolsCompleter( console ) );
        console.setConsoleCallback( new GroolsCallback( console, reasoner ) );
        console.start( );

    }

    private static final class GroolsCompleter implements Completion {
        private final Console console;

        private GroolsCompleter( final Console console ) {
            this.console = console;
        }


        @Override
        public void complete( final CompleteOperation completeOperation ) {
            final String line = completeOperation.getBuffer( ).replace( "\\s+", " " );
            final String[] tokens = line.split( " " );
            int spacePos = line.lastIndexOf( ' ' );
            long spaceNum = line.codePoints( ).filter( ch -> ch == ' ' ).count( );
            List< String > candidates = new ArrayList<>( );
            //System.out.printf( "space, count: %d last position: %d\n", spaceNum, spacePos );
            if ( spaceNum == 0 ) {
                if ( line.isEmpty( ) ) {
                    candidates.add( "get" );
                    candidates.add( "quit" );
                }
                else if ( completeOperation.getBuffer( ).startsWith( "g" ) )
                    candidates.add( "get" );
                else if ( completeOperation.getBuffer( ).startsWith( "q" ) )
                    candidates.add( "quit" );

            }
            else {
                completeOperation.setOffset( spacePos + console.getPrompt( ).getLength( ) - 1 );
                final String token = ( line.length( ) > spacePos ) ? line.substring( spacePos + 1 ) : "";
                if ( spaceNum == 1 ) {
                    if ( token.isEmpty( ) ) {
                        candidates.add( "concepts" );
                        candidates.add( "expectations" );
                        candidates.add( "observations" );
                        candidates.add( "predictions" );
                        candidates.add( "prior-knowledges" );
                        candidates.add( "quit" );
                        candidates.add( "relations" );
                    }
                    else if ( token.startsWith( "c" ) )
                        candidates.add( "concepts" );
                    else if ( token.startsWith( "e" ) )
                        candidates.add( "expectations" );
                    else if ( token.startsWith( "o" ) )
                        candidates.add( "observations" );
                    else if ( token.startsWith( "q" ) )
                        candidates.add( "quit" );
                    else if ( token.startsWith( "pri" ) )
                        candidates.add( "prior-knowledges" );
                    else if ( token.startsWith( "pre" ) )
                        candidates.add( "predictions" );
                    else if ( token.startsWith( "p" ) ) {
                        candidates.add( "predictions" );
                        candidates.add( "prior-knowledges" );
                    }
                    else if ( token.startsWith( "r" ) )
                        candidates.add( "relations" );
                }
                else if ( spaceNum == 2 ) {
                    candidates.add( "where" );
                }
                else if ( spaceNum == 3 ) {
                    switch ( tokens[ 1 ] ) {
                        case "prior-knowledges":
                            if ( token.isEmpty( ) ) {
                                candidates.add( "expectation" );
                                candidates.add( "conclusion" );
                                candidates.add( "name" );
                                candidates.add( "prediction" );
                            }
                            else if ( token.startsWith( "e" ) )
                                candidates.add( "expectation" );
                            else if ( token.startsWith( "c" ) )
                                candidates.add( "conclusion" );
                            else if ( token.startsWith( "n" ) )
                                candidates.add( "name" );
                            else if ( token.startsWith( "p" ) )
                                candidates.add( "prediction" );
                            break;
                        case "observations":
                        case "predictions":
                        case "expectations":
                        case "concepts":
                            candidates.add( "name" );
                            break;
                        case "relations":
                            if ( token.isEmpty( ) ) {
                                candidates.add( "source" );
                                candidates.add( "target" );
                            }
                            else if ( token.startsWith( "s" ) )
                                candidates.add( "source" );
                            else if ( token.startsWith( "t" ) )
                                candidates.add( "target" );
                            break;
                    }
                }
                else if ( spaceNum == 4 ) {
                    if ( token.isEmpty( ) ) {
                        candidates.add( "==" );
                        candidates.add( "!=" );
                        candidates.add( ">" );
                        candidates.add( ">=" );
                        candidates.add( "<" );
                        candidates.add( "<=" );
                    }
                    else if ( token.startsWith( "=" ) )
                        candidates.add( "==" );
                    else if ( token.startsWith( "!" ) )
                        candidates.add( "!=" );
                    else if ( token.startsWith( ">" ) ) {
                        candidates.add( ">" );
                        candidates.add( ">=" );
                    }
                    else if ( token.startsWith( "<" ) ) {
                        candidates.add( "<" );
                        candidates.add( "<=" );
                    }
                }
            }
            completeOperation.addCompletionCandidates( candidates );
        }
    }
}

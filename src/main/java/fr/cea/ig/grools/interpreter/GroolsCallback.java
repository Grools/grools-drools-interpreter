package fr.cea.ig.grools.interpreter;

import fr.cea.ig.grools.Reasoner;
import fr.cea.ig.grools.fact.Observation;
import fr.cea.ig.grools.fact.PriorKnowledge;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleCallback;
import org.jboss.aesh.console.ConsoleOperation;
import org.jboss.aesh.console.Process;
import org.jboss.aesh.console.command.CommandOperation;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * GroolsCallback
 */
final class GroolsCallback implements ConsoleCallback {
    private static final Pattern query = Pattern.compile( "get\\s+(\\w+[[\\-]?\\w+]*)\\s+where\\s+(\\w+)\\s+([=!><]{2})\\s+([^\\s]+)" );
    private final Console console;
    private final Reasoner reasoner;

    GroolsCallback( final Console console, final Reasoner reasoner ) {
        this.console = console;
        this.reasoner = reasoner;
    }

    @Override
    public int execute( ConsoleOperation output ) throws InterruptedException {
        final String input = output.getBuffer( );
        if ( input.startsWith( "quit" ) )
            console.stop( );
        else {
            final Matcher matcher = query.matcher( input );
            final Set< Object > objects = new HashSet<>( );
            if ( matcher.find( ) ) {
                if ( matcher.group( 1 ).equals( "concepts" ) ) {
                    if ( matcher.group( 2 ).equals( "name" ) ) {
                        if ( matcher.group( 3 ).equals( "==" ) ) {
                            objects.add( reasoner.getConcept( matcher.group( 4 ) ) );
                        }
                        else if ( matcher.group( 3 ).equals( "!=" ) ) {
                            reasoner.getConcepts( )
                                    .stream( )
                                    .filter( c -> !c.getName( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else
                            System.err.println( "Unsupported query using symbol: " + matcher.group( 3 ) );
                    }
                    else if ( matcher.group( 2 ).equals( "source" ) ) {
                        if ( matcher.group( 3 ).equals( "==" ) ) {
                            reasoner.getConcepts( )
                                    .stream( )
                                    .filter( c -> c.getSource( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toSet( ) );
                        }
                        else if ( matcher.group( 3 ).equals( "!=" ) ) {
                            reasoner.getConcepts( )
                                    .stream( )
                                    .filter( c -> !c.getSource( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else
                            System.err.println( "Unsupported query using symbol: " + matcher.group( 3 ) );
                    }
                    else {
                        System.err.println( "Unsupported query using field: " + matcher.group( 2 ) );
                    }
                }
                else if ( matcher.group( 1 ).equals( "prior-knowledges" ) ) {
                    if ( matcher.group( 2 ).equals( "name" ) ) {
                        if ( matcher.group( 3 ).equals( "==" ) ) {
                            final PriorKnowledge pk = reasoner.getPriorKnowledge( matcher.group( 4 ) );
                            if ( pk != null )
                                objects.add( pk );
                        }
                        else if ( matcher.group( 3 ).equals( "!=" ) ) {
                            reasoner.getPriorKnowledges( )
                                    .stream( )
                                    .filter( pk -> pk.getName( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else
                            System.err.println( "Unsupported query using symbol: " + matcher.group( 3 ) );
                    }
                    else if ( matcher.group( 2 ).equals( "source" ) ) {
                        if ( matcher.group( 3 ).equals( "==" ) ) {
                            reasoner.getPriorKnowledges( )
                                    .stream( )
                                    .filter( pk -> pk.getSource( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else if ( matcher.group( 3 ).equals( "!=" ) ) {
                            reasoner.getPriorKnowledges( ).stream( )
                                    .filter( pk -> !pk.getSource( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else
                            System.err.println( "Unsupported query using symbol: " + matcher.group( 3 ) );
                    }
                    else {
                        System.err.println( "Unsupported query using field: " + matcher.group( 2 ) );
                    }

                }
                else if ( matcher.group( 1 ).equals( "relations" ) ) {
                    if ( matcher.group( 2 ).equals( "source" ) ) {
                        if ( matcher.group( 3 ).equals( "==" ) ) {
                            reasoner.getRelations( )
                                    .stream( )
                                    .filter( r -> r.getSource( ).getName( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else if ( matcher.group( 3 ).equals( "!=" ) ) {
                            reasoner.getRelations( )
                                    .stream( )
                                    .filter( r -> !r.getSource( ).getName( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else
                            System.err.println( "Unsupported query using symbol: " + matcher.group( 3 ) );
                    }
                    else if ( matcher.group( 2 ).equals( "target" ) ) {
                        if ( matcher.group( 3 ).equals( "==" ) ) {
                            reasoner.getRelations( )
                                    .stream( )
                                    .filter( r -> r.getTarget( ).getName( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else if ( matcher.group( 3 ).equals( "!=" ) ) {
                            reasoner.getRelations( ).stream( )
                                    .filter( r -> !r.getTarget( ).getName( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else
                            System.err.println( "Unsupported query using symbol: " + matcher.group( 3 ) );
                    }
                    else {
                        System.err.println( "Unsupported query using field: " + matcher.group( 2 ) );
                    }

                }
                else if ( matcher.group( 1 ).equals( "observations" ) ) {
                    if ( matcher.group( 2 ).equals( "name" ) ) {
                        if ( matcher.group( 3 ).equals( "==" ) ) {
                            final Observation observation = reasoner.getObservation( matcher.group( 4 ) );
                            if ( observation != null )
                                objects.add( observation );
                        }
                        else if ( matcher.group( 3 ).equals( "!=" ) ) {
                            reasoner.getObservations( ).stream( )
                                    .filter( o -> !o.getName( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else
                            System.err.println( "Unsupported query using symbol: " + matcher.group( 3 ) );
                    }
                    else if ( matcher.group( 2 ).equals( "source" ) ) {
                        if ( matcher.group( 3 ).equals( "==" ) ) {
                            reasoner.getObservations( )
                                    .stream( )
                                    .filter( o -> o.getSource( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else if ( matcher.group( 3 ).equals( "!=" ) ) {
                            reasoner.getObservations( ).stream( )
                                    .filter( o -> !o.getSource( ).equals( matcher.group( 4 ) ) )
                                    .collect( Collectors.toCollection( ( ) -> objects ) );
                        }
                        else
                            System.err.println( "Unsupported query using symbol: " + matcher.group( 3 ) );
                    }
                    else {
                        System.err.println( "Unsupported query using field: " + matcher.group( 2 ) );
                    }

                }
                else {
                    System.err.println( "Unsupported query using type: " + matcher.group( 1 ) );
                }

                if ( objects.isEmpty( ) )
                    System.out.println( "Any concepts match the constrain" );
                else
                    objects.forEach( System.out::println );
            }
            else
                System.err.println( "Unsuported expression: " + input );
        }

        return 0;
    }

    @Override
    public CommandOperation getInput( ) throws InterruptedException {
        return console.getConsoleCallback( ).getInput( );
    }

    @Override
    public String getInputLine( ) throws InterruptedException {
        return console.getInputLine( );
    }

    @Override
    public void setProcess( Process process ) {
    }

}

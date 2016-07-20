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
import fr.cea.ig.grools.Mode;
import fr.cea.ig.grools.Reasoner;
import fr.cea.ig.grools.drools.ReasonerImpl;
import fr.cea.ig.grools.fact.Concept;
import fr.cea.ig.grools.fact.Observation;
import fr.cea.ig.grools.fact.PriorKnowledge;
import fr.cea.ig.grools.fact.Relation;
import lombok.NonNull;
import org.kie.api.KieBase;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyAcceptor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.marshalling.MarshallerFactory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private final static Logger LOGGER  = (Logger) LoggerFactory.getLogger(Main.class);
    private final static String APPNAME = "grools-drools-interpreter";

    private static Marshaller createMarshaller( @NonNull final KieBase kBase ){
        final ObjectMarshallingStrategyAcceptor acceptor    = MarshallerFactory.newClassFilterAcceptor(new String[] { "*.*" });
        final ObjectMarshallingStrategy         strategy    = MarshallerFactory.newSerializeMarshallingStrategy(acceptor);
        final Marshaller                        marshaller  = MarshallerFactory.newMarshaller(kBase, new ObjectMarshallingStrategy[] { strategy });
        return marshaller;
    }

    private static Reasoner load(@NonNull final File file){
        Reasoner                reasoner = null;
        final FileInputStream   fis;
        try {
            fis = new FileInputStream(file);
            final ObjectInputStream         ois         = new ObjectInputStream(fis);
            final KieBase                   kBase       = (KieBase) ois.readObject();
            final KieSessionConfiguration   kconf       = (KieSessionConfiguration) ois.readObject();
            final Mode                      mode        = (Mode) ois.readObject();
            final Marshaller                marshaller  = createMarshaller(kBase);
            KieSession                      kieSession  = marshaller.unmarshall(fis, kconf, null);
            reasoner = new ReasonerImpl(kBase, kieSession, mode );
        } catch ( ClassNotFoundException e ) {
            LOGGER.error( e.getMessage() );
            System.exit(1);
        } catch ( FileNotFoundException e ) {
            LOGGER.error("File: " + file.toString() + "not found");
            System.exit(1);
        } catch ( IOException e ) {
            LOGGER.error("Can not read/write into " + file.toString());
            System.exit(1);
        }
        return reasoner;
    }

    private static void showHelp(){
        System.out.println( APPNAME+" grools_file" );
        System.out.println( APPNAME+" -h        display this help" );
    }

    public static void main(String[] args) {
        //args = new String[]{"/media/sf_agc/proj/Grools/res/UP000000430-AbaylyiADP1/GenomeProperties/reasoner.grools"};
        if( args.length != 1 ){
            System.err.println("Error "+APPNAME+" needs a grools file");
            showHelp();
            System.exit(1);
        }
        else if( args[0].equals("-h") ){
            showHelp();
            System.exit(0);
        }

        final Reasoner reasoner = load( new File(args[0]) );

        boolean isRunning = true;
        final Scanner scanner = new Scanner(System.in);
        System.out.println("=== Query shell interpreter ===");
        System.out.println("Enter quit to leave");
        final Pattern query = Pattern.compile("get\\s+(\\w+[[\\-]?\\w+]*)\\s+where\\s+(\\w+)\\s+([=!]{2})\\s+([^\\s]+)");
        while ( isRunning ){
            System.out.print("> ");
            final String  input     = scanner.nextLine();
            if( input.equals("quit") )
                isRunning=false;
            else{
                final Matcher matcher   = query.matcher(input);
                if( matcher.find() ) {
                    if ( matcher.group(1).equals("concepts") ) {
                        Set<Concept> concepts = null;
                        if ( matcher.group(2).equals("name") ) {
                            if ( matcher.group(3).equals("==") ) {
                                concepts = new HashSet<>();
                                concepts.add(reasoner.getConcept(matcher.group(4)));
                            } else if ( matcher.group(3).equals("!=") ) {
                                concepts = reasoner.getConcepts().stream()
                                                   .filter(c -> ! c.getName().equals(matcher.group(4)))
                                                   .collect(Collectors.toSet());
                            } else
                                System.err.println("Unsupported query using symbol: " + matcher.group(3));
                        } else if ( matcher.group(2).equals("source") ) {
                            if ( matcher.group(3).equals("==") ) {
                                concepts = reasoner.getConcepts().stream()
                                                   .filter(c -> c.getSource().equals(matcher.group(4)))
                                                   .collect(Collectors.toSet());
                            } else if ( matcher.group(3).equals("!=") ) {
                                concepts = reasoner.getConcepts().stream()
                                                   .filter(c -> ! c.getSource().equals(matcher.group(4)))
                                                   .collect(Collectors.toSet());
                            } else
                                System.err.println("Unsupported query using symbol: " + matcher.group(3));
                        } else {
                            System.err.println("Unsupported query using field: " + matcher.group(2));
                        }
                        if( concepts == null || concepts.isEmpty() )
                            System.out.println("Any concepts match the constrain");
                        else
                            concepts.stream().forEach(System.out::println);
                    } else if ( matcher.group(1).equals("prior-knowledges") ) {
                        Set<PriorKnowledge> priorKnowledges = null;
                        if ( matcher.group(2).equals("name") ) {
                            if ( matcher.group(3).equals("==") ) {
                                final PriorKnowledge pk = reasoner.getPriorKnowledge(matcher.group(4));
                                if( pk != null ) {
                                    priorKnowledges = new HashSet<>();
                                    priorKnowledges.add(pk);
                                }
                            } else if ( matcher.group(3).equals("!=") ) {
                                priorKnowledges = reasoner.getPriorKnowledges().stream()
                                                          .filter(pk -> pk.getName().equals(matcher.group(4)))
                                                          .collect(Collectors.toSet());
                            } else
                                System.err.println("Unsupported query using symbol: " + matcher.group(3));
                        } else if ( matcher.group(2).equals("source") ) {
                            if ( matcher.group(3).equals("==") ) {
                                priorKnowledges = reasoner.getPriorKnowledges().stream()
                                                          .filter(pk -> pk.getSource().equals(matcher.group(4)))
                                                          .collect(Collectors.toSet());
                            } else if ( matcher.group(3).equals("!=") ) {
                                priorKnowledges = reasoner.getPriorKnowledges().stream()
                                                          .filter(pk -> ! pk.getSource().equals(matcher.group(4)))
                                                          .collect(Collectors.toSet());
                            } else
                                System.err.println("Unsupported query using symbol: " + matcher.group(3));
                        } else {
                            System.err.println("Unsupported query using field: " + matcher.group(2));
                        }
                        if( priorKnowledges == null || priorKnowledges.isEmpty() )
                            System.out.println("Any prior-knowledges match the constrain");
                        else
                            priorKnowledges.stream().forEach(System.out::println);

                    } else if ( matcher.group(1).equals("relations") ) {
                        Set<Relation> relations = null;
                        if ( matcher.group(2).equals("source") ) {
                            if ( matcher.group(3).equals("==") ) {
                                relations = reasoner.getRelations().stream()
                                                    .filter(r -> r.getSource().getName().equals(matcher.group(4)))
                                                    .collect(Collectors.toSet());
                            } else if ( matcher.group(3).equals("!=") ) {
                                relations = reasoner.getRelations().stream()
                                                    .filter(r -> ! r.getSource().getName().equals(matcher.group(4)))
                                                    .collect(Collectors.toSet());
                            } else
                                System.err.println("Unsupported query using symbol: " + matcher.group(3));
                        } else if ( matcher.group(2).equals("target") ) {
                            if ( matcher.group(3).equals("==") ) {
                                relations = reasoner.getRelations().stream()
                                                    .filter(r -> r.getTarget().getName().equals(matcher.group(4)))
                                                    .collect(Collectors.toSet());
                            } else if ( matcher.group(3).equals("!=") ) {
                                relations = reasoner.getRelations().stream()
                                                    .filter(r -> ! r.getTarget().getName().equals(matcher.group(4)))
                                                    .collect(Collectors.toSet());
                            } else
                                System.err.println("Unsupported query using symbol: " + matcher.group(3));
                        } else {
                            System.err.println("Unsupported query using field: " + matcher.group(2));
                        }
                        if( relations == null || relations.isEmpty() )
                            System.out.println("Any relations match the constrain");
                        else
                            relations.stream().forEach(System.out::println);

                    } else if ( matcher.group(1).equals("observations") ) {
                        Set<Observation> observations = null;
                        if ( matcher.group(2).equals("name") ) {
                            if ( matcher.group(3).equals("==") ) {
                                final Observation observation = reasoner.getObservation(matcher.group(4));
                                if( observation != null ){
                                    observations = new HashSet<>();
                                    observations.add(observation);
                                }
                            } else if ( matcher.group(3).equals("!=") ) {
                                observations = reasoner.getObservations().stream()
                                                       .filter(o -> ! o.getName().equals(matcher.group(4)))
                                                       .collect(Collectors.toSet());
                            } else
                                System.err.println("Unsupported query using symbol: " + matcher.group(3));
                        } else if ( matcher.group(2).equals("source") ) {
                            if ( matcher.group(3).equals("==") ) {
                                observations = reasoner.getObservations().stream()
                                                       .filter(o -> o.getSource().equals(matcher.group(4)))
                                                       .collect(Collectors.toSet());
                            } else if ( matcher.group(3).equals("!=") ) {
                                observations = reasoner.getObservations().stream()
                                                       .filter(o -> ! o.getSource().equals(matcher.group(4)))
                                                       .collect(Collectors.toSet());
                            } else
                                System.err.println("Unsupported query using symbol: " + matcher.group(3));
                        } else {
                            System.err.println("Unsupported query using field: " + matcher.group(2));
                        }
                        if( observations == null || observations.isEmpty() )
                            System.out.println("Any relations match the constrain");
                        else
                            observations.stream().forEach(System.out::println);

                    } else {
                        System.err.println("Unsupported query using type: " + matcher.group(1));
                    }
                }
                else
                    System.err.println("Unsuported expression: "+input);
            }
        }
    }
}

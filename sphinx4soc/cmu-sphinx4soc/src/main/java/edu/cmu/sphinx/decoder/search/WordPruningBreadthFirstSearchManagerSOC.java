/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.sphinx.decoder.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.decoder.pruner.Pruner;
import edu.cmu.sphinx.decoder.scorer.AcousticScorer;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.linguist.Linguist;
import edu.cmu.sphinx.linguist.SearchGraph;
import edu.cmu.sphinx.linguist.SearchState;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.LogMath;
import edu.cmu.sphinx.util.StatisticsVariable;
import edu.cmu.sphinx.util.TimerPool;

/**
 *
 * @author gorg
 */
public class WordPruningBreadthFirstSearchManagerSOC extends WordPruningBreadthFirstSearchManager
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public WordPruningBreadthFirstSearchManagerSOC ( LogMath logMath, Linguist linguist, Pruner pruner,
                                                     AcousticScorer scorer, ActiveListManager activeListManager,
                                                     boolean showTokenCount, double relativeWordBeamWidth,
                                                     int growSkipInterval, boolean checkStateOrder, boolean buildWordLattice,
                                                     int maxLatticeEdges, float acousticLookaheadFrames,
                                                     boolean keepAllTokens )
    {
        super( logMath, linguist, pruner, scorer, activeListManager, showTokenCount, relativeWordBeamWidth,
                growSkipInterval, checkStateOrder, buildWordLattice, maxLatticeEdges, acousticLookaheadFrames, keepAllTokens );
    }

    public void startRecognitionSOC ( SearchGraph searchGraph )
    {
        numStateOrder = searchGraph.getNumStateOrder();
        currentFrameNumber = 0;
        curTokensScored.value = 0;
        
        activeListManager.setNumStateOrder( numStateOrder );
        if ( buildWordLattice ) {
            loserManager = new AlternateHypothesisManager( maxLatticeEdges );
        }

        SearchState state = searchGraph.getInitialState();

        activeList = activeListManager.getEmittingList();
        activeList.add( new Token( state, currentFrameNumber ) );

        clearCollectors();

        growBranches();
        growNonEmittingBranches();
    }

       public void stopRecognitionSOC ()
    {
//        log.info( "Stoping" );
        localStop();
    }

    @Override
    public void allocate ()
    {
//        log.info( "Allocating" );
        scoreTimer = TimerPool.getTimer( this, "Score" );
        pruneTimer = TimerPool.getTimer( this, "Prune" );
        growTimer = TimerPool.getTimer( this, "Grow" );

        totalTokensScored = StatisticsVariable.getStatisticsVariable( "totalTokensScored" );
        curTokensScored = StatisticsVariable.getStatisticsVariable( "curTokensScored" );
        tokensCreated = StatisticsVariable.getStatisticsVariable( "tokensCreated" );

    }

    @Override
    public ActiveList getActiveList ()
    {
        activeList = activeListManager.getEmittingList();
        return activeList;
    }

    public boolean recognizeSoA ( Data token )
    {
        boolean moreTokens;
//        logger.debug( token.toString() );
        Token bestToken = null;
        if ( token instanceof Token ) {
            bestToken = (Token)token;
        } else if ( token instanceof DataEndSignal ) {
            streamEnd = true;
            return false;
        } else if ( token instanceof DataStartSignal ) {
            return true;
        }
        
        moreTokens = ( bestToken!=null );

//        logger.info( "size of activelist = " + activeList.size() );
        activeList.setBestToken( bestToken );

        //monitorWords(activeList); 
        monitorStates( activeList );

        // System.out.println("BEST " + bestToken);

        curTokensScored.value += activeList.size();
        totalTokensScored.value += activeList.size();

        if ( moreTokens ) {
            pruneBranches();
            currentFrameNumber++;
            if ( growSkipInterval==0||( currentFrameNumber%growSkipInterval )!=0 ) {
                clearCollectors();
                growEmittingBranches();
                growNonEmittingBranches();
            }
        }
        return moreTokens;
    }

    public Result createResult ( boolean done )
    {
//        logger.info( resultList.toString() );
        Result result = new Result( loserManager, activeList, resultList, currentFrameNumber, true, logMath );
        logger.info( "Result: " + result.getBestFinalResultNoFiller() );
        // tokenTypeTracker.show();
        if ( showTokenCount ) {
            showTokenCount();
        }
        
        return result;
    }

    @Override
    public void stopRecognition ()
    {
        localStop();
    }

    @Override
    public void deallocate ()
    {
//        log.info( "Deallocating" );
    }
}

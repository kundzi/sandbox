package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static edu.rice.pcdp.PCDP.finish;
import static edu.rice.pcdp.config.SystemProperty.numWorkers;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 * <p>
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
  /**
   * {@inheritDoc}
   * <p>
   * TODO Use the SieveActorActor class to calculate the number of primes <=
   * limit in parallel. You might consider how you can model the Sieve of
   * Eratosthenes as a pipeline of actors, each corresponding to a single
   * prime number.
   */
  @Override
  public int countPrimes(final int limit) {
      numWorkers.setProperty(
          String.valueOf(Runtime.getRuntime().availableProcessors()));

      final SieveActorActor actor = new SieveActorActor(2);
      finish(() -> {
        for (int i = 3; i < limit; i += 2) {
          actor.send(i);
        }
        actor.send(0);
      });

      int numPrimes = 0;
      SieveActorActor tmpActor = actor;
      while (null != tmpActor) {
        numPrimes += tmpActor.numLocalPrimes;
        tmpActor = tmpActor.nextActor;
      }
      return numPrimes;
  }

  /**
   * An actor class that helps implement the Sieve of Eratosthenes in
   * parallel.
   */
  public static final class SieveActorActor extends Actor {

    private static final int MAX_LOCAL_PRIMES = 1_000;
    private final int localPrimes[];

    private int numLocalPrimes;
    private SieveActorActor nextActor;

    public SieveActorActor(final int localPrime) {
      this.localPrimes = new int[MAX_LOCAL_PRIMES];
      this.localPrimes[0] = localPrime;
      this.numLocalPrimes = 1;
      this.nextActor = null;
    }

    /**
     * Process a single message sent to this actor.
     * <p>
     * TODO complete this method.
     *
     * @param msg Received message
     */
    @Override
    public void process(final Object msg) {
      final int candidate = (Integer) msg;
      if (candidate < 0) {
        throw new IllegalArgumentException(
            "WTF: " + candidate + " but must be non-negative");
      }

      if (0 == candidate && null != nextActor) {
        nextActor.send(candidate);
        return;
      }

      final boolean locallyPrime = isLocallyPrime(candidate);
      if (!locallyPrime) {
        return;
      }

      if (numLocalPrimes < MAX_LOCAL_PRIMES) {
        localPrimes[numLocalPrimes] = candidate;
        numLocalPrimes++;
      } else if (null == nextActor) {
        nextActor = new SieveActorActor(candidate);
      } else {
        nextActor.send(candidate);
      }
    }

    private boolean isLocallyPrime(final int candidate) {
      final boolean[] isPrime = {true};
      checkPrimeKernel(candidate, isPrime, 0, numLocalPrimes);
      return isPrime[0];
    }

    private void checkPrimeKernel(final int candidate,
                                  final boolean[] isPrime,
                                  final int startIndex,
                                  final int endIndex) {
      for (int i = startIndex; i < endIndex; i++) {
        if (candidate % localPrimes[i] == 0) {
          isPrime[0] = false;
          break;
        }
      }
    }
  }
}

//
// Hidden Markov Model program for metamorphic virus
//
// The program reads in data (virus assembly opcodes) ignoring
// registers, addresses, labels, comments, and white space.
// The A matrix is N x N and the B matrix is N x M, where M,
// the number of observation symbols (opcodes), is set equal 
// to the number of distinct opcodes seen in the training set. 
// The program begins with random (approximately uniform) A, 
// B and pi, then attempts to climb to the optimal A and B.
// This program implements the work described in Cave and 
// Neuwirth's paper "Hidden Markov Models for English"
//


#include "hmm.h"
#include <string.h>

int main(int argc, const char *argv[])
{
    int N,
		    M,
		    T,
		    maxIters,
		    seed,
		    i,
		    j,
		    iter,
        str_len;

    char **alphabet;

    double logProb,
           newLogProb;

  	double *pi,
           *piBar, 
           **A,
           **Abar,
           **B,
           **Bbar;

    struct stepStruct *step;

    FILE *in,
         *out;
    
    char s[80];

	int wantTraining = 1;
    
    if(argc != 10)
    {
        fprintf(stderr, "\nUsage: %s N M T maxIters filename alphabet modelfile seed\n\n", argv[0]);
        fprintf(stderr, "where N == number of states of the HMM\n");
        fprintf(stderr, "      M == number of observation symbols\n");
        fprintf(stderr, "      T == number of observations in the training set\n");
        fprintf(stderr, "      maxIters == max iterations of re-estimation algorithm\n");
        fprintf(stderr, "      filename == name of input file\n");
        fprintf(stderr, "      alphabet == name of file defining the alphabet\n");
        fprintf(stderr, "      modelfile == name of model output file\n");
        fprintf(stderr, "      seed == seed value for pseudo-random number generator (PRNG)\n\n");
		fprintf(stderr, "      wantTraining == to train enter 1, otherwise 0 \n\n");
        fprintf(stderr, "For example:\n\n      %s 2 10 10000 500 datafile alphabet modelfile 1241\n\n", argv[0]);
        fprintf(stderr, "will create a HMM with 2 states and 10 observation symbols,\n");
        fprintf(stderr, "will read in the first 10000 observations from `datafile',\n");
        fprintf(stderr, "will use the observation symbols defined in file `alphabet', and\n");
        fprintf(stderr, "will write the model (pi, A, B) to `modelfile', and\n");
        fprintf(stderr, "will seed the PRNG with 1241 and train the HMM with a maximum of 500 iterations.\n\n");
        exit(0);
    }

    N = atoi(argv[1]);
    M = atoi(argv[2]);
    T = atoi(argv[3]);
    maxIters = atoi(argv[4]);
    seed = atoi(argv[8]);
	wantTraining = atoi(argv[9]);

    pi = (double *)malloc(N * sizeof(double));
    piBar = (double *)malloc(N * sizeof(double));

    A = (double **)malloc(N * sizeof(double*));
    Abar =static_cast<double **>(malloc(N * sizeof(double*)));
    for (i=0; i<N; ++i)
    {
      A[i] = static_cast<double *>(malloc(N * sizeof(double)));
      Abar[i] = static_cast<double *>(malloc(N * sizeof(double)));
    }

    B = static_cast<double **>(malloc(N * sizeof(double*)));
    Bbar = static_cast<double **>(malloc(N * sizeof(double*)));
    for (i=0; i<N; ++i)
    {
      B[i] = static_cast<double *>(malloc(M * sizeof(double)));
      Bbar[i] = static_cast<double *>(malloc(M * sizeof(double)));
    }
    
   
    ////////////////////////
    // read the data file //
    ////////////////////////

    // allocate memory
    printf("allocating %d bytes of memory... ", (T + 1) * sizeof(struct stepStruct));
    fflush(stdout);
    if((step = static_cast<stepStruct *>(calloc(T + 1, sizeof(struct stepStruct)))) == NULL)
    {
        fprintf(stderr, "\nUnable to allocate alpha\n\n");
        exit(0);
    }
    for (i=0; i<T+1; ++i)
    {
      step[i].alpha = static_cast<double *>(malloc(N * sizeof(double)));
      step[i].beta = static_cast<double *>(malloc(N * sizeof(double)));
      step[i].gamma = static_cast<double *>(malloc(N * sizeof(double)));
      step[i].diGamma = static_cast<double **>(malloc(N * sizeof(double*)));
      for (j=0; j<N; ++j)
      {
        step[i].diGamma[j] = static_cast<double *>(malloc(N * sizeof(double)));
      }
    }
    printf("done\n");

    // read in the observations from file
    printf("GetObservations... ");
    fflush(stdout);
    in = fopen(argv[5], "r"); // argv[5] = filename
    if(in == NULL)
    {
        fprintf(stderr, "\nError opening file %s\n\n", argv[5]);
        exit(0);
    }
    i = 0;
    fgets(s,80,in); // get rid of the first line
    while (i < T)
    {
      fgets(s,80,in);
      step[i].obs = atoi(s);
      ++i;
    }
    fclose(in);
    printf("done\n");

    // read in the alphabet from file
    printf("GetAlphabet... ");
    fflush(stdout);
    alphabet = static_cast<char **>(malloc(M * sizeof (char*)));
    in = fopen(argv[6], "r"); // argv[6] = alphabet
    if(in == NULL)
    {
        fprintf(stderr, "\nError opening file %s\n\n", argv[6]);
        exit(0);
    }
    i = 0;
    fgets(s,80,in); // get rid of the first line
    while (i < M)
    {
      fgets(s,80,in);
	    str_len = strlen(s);
      alphabet[i] = static_cast<char *>(malloc(str_len * sizeof(char)));
      strncpy(alphabet[i], s, str_len-1);
      alphabet[i][str_len-1] = '\0';
      ++i;
    }
    fclose(in);
    printf("done\n");


    /////////////////////////
    // hidden markov model //
    /////////////////////////

    srand(seed);

    // initialize pi[], A[][] and B[][]
    initMatrices(pi, A, B, N, M, seed);

    // print pi[], A[][] and B[][] transpose
    printf("\nN = %d, M = %d, T = %d\n", N, M, T);
    printf("initial pi =\n");
    printPi(pi, N);
    printf("initial A =\n");
    printA(A, N);
    printf("initial B^T =\n");
    printBT(B, N, M, alphabet);

    // initialization
    iter = 0;
    logProb = -1.0;
    newLogProb = 0.0;

	if (wantTraining) {

		// main loop
		while((iter < maxIters) && (newLogProb > logProb))
		{
			printf("\nbegin iteration = %d\n", iter);

			logProb = newLogProb;

			// alpha (or forward) pass
			printf("alpha pass... ");
			fflush(stdout);
			alphaPass(step, pi, A, B, N, T);
			printf("done\n");

			// beta (or backwards) pass
			printf("beta pass... ");
			fflush(stdout);
			betaPass(step, pi, A, B, N, T);
			printf("done\n");

			// compute gamma's and diGamma's
			printf("compute gamma's and diGamma's... ");
			fflush(stdout);
			computeGammas(step, pi, A, B, N, T);
			printf("done\n");

			// find piBar, reestimate of pi
			printf("reestimate pi... ");
			fflush(stdout);
			reestimatePi(step, piBar, N);
			printf("done\n");

			// find Abar, reestimate of A
			printf("reestimate A... ");
			fflush(stdout);
			reestimateA(step, Abar, N, T);
			printf("done\n");

			// find Bbar, reestimate of B
			printf("reestimate B... ");
			fflush(stdout);
			reestimateB(step, Bbar, N, M, T);
			printf("done\n");

	#ifdef PRINT_REESTIMATES
			printf("piBar =\n");
			printPi(piBar, N);
			printf("Abar =\n");
			printA(Abar, N);
			printf("Bbar^T = \n");
			printBT(Bbar, N, M, alphabet);
	#endif // PRINT_REESTIMATES

			// assign pi, A and B corresponding "bar" values
			for(i = 0; i < N; ++i)
			{
				pi[i] = piBar[i];

				for(j = 0; j < N; ++j)
				{
					A[i][j] = Abar[i][j];
				}

				for(j = 0; j < M; ++j)
				{
					B[i][j] = Bbar[i][j];
				}

			}// next i

			// compute log [P(observations | lambda)], where lambda = (A,B,pi)
			newLogProb = 0.0;
			for(i = 0; i < T; ++i)
			{
				newLogProb += log(step[i].c);
			}
			newLogProb = -newLogProb;

			// a little trick so that no initial logProb is required
			if(iter == 0)
			{
				logProb = newLogProb - 1.0;
			}

			printf("completed iteration = %d, log [P(observation | lambda)] = %f\n",
					iter, newLogProb);

			++iter;

		}// end while
    
		out = fopen(argv[7], "w"); // argv[7] = modelfile
		writeModel(pi, A, B, N, M, T, alphabet, out);
		fclose(out);
    
		printf("\nT = %d, N = %d, M = %d, iterations = %d\n\n", T, N, M, iter);
		printf("final pi =\n");
		printPi(pi, N);
		printf("\nfinal A =\n");
		printA(A, N);
		printf("\nfinal B^T =\n");
		printBT(B, N, M, alphabet);
		printf("\nlog [P(observations | lambda)] = %f\n\n", newLogProb);

	} // end of training
	else { //want to do testing


		out = fopen(argv[7], "r"); // argv[7] = modelfile
		readModelFile(pi, A, B, N, M, T, alphabet, out);
		
		// alpha (or forward) pass
		printf("alpha pass... ");
		fflush(stdout);
		alphaPass(step, pi, A, B, N, T);
		printf("done\n");
		
		
	//	FILE * newFile = fopen("testing.txt", "a");
		//writeModel(pi, A, B, N, M, T, alphabet, newFile);

		//fclose(newFile);

		fclose(out);
	} // end of testing
}// end hmm


// read model file
void readModelFile(double *pi, 
                double **A, 
                double **B, 
                int N, 
                int M, 
                int T, 
                char **alphabet,
                FILE *filename )
{
	int N1, M1, T1;
	char str[80];
	//FILE *in = fopen(filename, "r");
	if (filename != NULL) 
	{	
		fscanf(filename, "N=%d, M=%d, T=%d\n", &N1, &M1, &T1);
		if (N1 != N || M1 != M || T != T1) {
			fprintf(stderr, "\nN, M, or T is not the same parameters!\n\n");
			exit(0);
		}

		fgets(str, sizeof(str), filename); //I:
		readPi(pi, N, filename);
		while (strstr(str, "A:")==NULL)
			fgets(str, sizeof(str), filename); //A:
		readA(A, N, filename);
		while (strstr(str, "B:")==NULL)
			fgets(str, sizeof(str), filename); //B:
		readBT(B, N, M, alphabet, filename);
	}
	//fclose(filename);
}

//
// read pi[] to file 
//
void readPi(double *pi, int N, FILE *in)
{
    int i;

    for(i = 0; i < N; ++i)
    {
        fscanf(in, "%lf", &pi[i]);
    }


}// end readPi


//
// read A[][]
//
void readA(double **A, int N, FILE * in)
{
    int i,
        j;

    double ftemp;

    for(i = 0; i < N; ++i)
    {   
		for(j = 0; j < N; ++j)
			fscanf(in, "%lf", &A[i][j]);       

    }// next i

}// end readA


//
// read BT[][]
//
void readBT(double **B, int N, int M, char **alphabet, FILE * in)
{
    int i,
        j;

    double ftemp;

//    char alphabet[M] = ALPHABET;

    for(i = 0; i < M; ++i)
    {
		fscanf(in, "%s", alphabet[i]);
		for(j = 0; j < N; ++j)
			fscanf(in, "%lf", &B[j][i]);
    }


}// end readB


double computeLogProb(struct stepStruct *step, int T) {
	double newlogProb = 0.0;
	int i;

	for(i = 0; i < T; ++i) {
		newlogProb += log(step[i].c);
	}
	newlogProb = -newlogProb;
	return newlogProb;
}

//
// alpha pass (or forward pass) including scaling
//
void alphaPass(struct stepStruct *step,
               double *pi,
               double **A,
               double **B,
               int N,
               int T)
{
    int i,
        j,
        t;

    double ftemp;

    // compute alpha[0]'s
    ftemp = 0.0;
    for(i = 0; i < N; ++i)
    {
        step[0].alpha[i] = pi[i] * B[i][step[0].obs];
        ftemp += step[0].alpha[i];
    }
    step[0].c = 1.0 / ftemp;

    // scale alpha[0]'s
    for(i = 0; i < N; ++i)
    {
        step[0].alpha[i] /= ftemp;
    }

    // alpha pass
    for(t = 1; t < T; ++t)
    {
        ftemp = 0.0;
        for(i = 0; i < N; ++i)
        {
            step[t].alpha[i] = 0.0;
            for(j = 0; j < N; ++j)
            {
                step[t].alpha[i] += step[t - 1].alpha[j] * A[j][i];
            }
            step[t].alpha[i] *= B[i][step[t].obs];
            ftemp += step[t].alpha[i];
        }
        step[t].c = 1.0 / ftemp;

        // scale alpha's
        for(i = 0; i < N; ++i)
        {
            step[t].alpha[i] /= ftemp;
        }

    }// next t
	printf("logProb %f\n", computeLogProb(step, T)/T);
	

}// end alphaPass


//
// beta pass (or backwards pass) including scaling
//
void betaPass(struct stepStruct *step,
              double *pi,
              double **A,
              double **B,
              int N,
              int T)
{
    int i,
        j,
        t;

    // compute scaled beta[T - 1]'s
    for(i = 0; i < N; ++i)
    {
        step[T - 1].beta[i] = 1.0 * step[T - 1].c;
    }

    // beta pass
    for(t = T - 2; t >= 0; --t)
    {
        for(i = 0; i < N; ++i)
        {
            step[t].beta[i] = 0.0;
            for(j = 0; j < N; ++j)
            {
                step[t].beta[i] += A[i][j] * B[j][step[t + 1].obs] * step[t + 1].beta[j];
            }

            // scale beta's (same scale factor as alpha's)
            step[t].beta[i] *= step[t].c;
        }

    }// next t

}// end betaPass


//
// compute gamma's and diGamma's including optional error checking
//
void computeGammas(struct stepStruct *step,
                   double *pi,
                   double **A,
                   double **B,
                   int N,
                   int T)
{
    int i,
        j,
        t;

    double denom;

#ifdef CHECK_GAMMAS
    double ftemp,
           ftemp2;
#endif // CHECK_GAMMAS

    // compute gamma's and diGamma's
    for(t = 0; t < T - 1; ++t)
    {
        denom = 0.0;
        for(i = 0; i < N; ++i)
        {
            for(j = 0; j < N; ++j)
            {
                denom += step[t].alpha[i] * A[i][j] * B[j][step[t + 1].obs] * step[t + 1].beta[j];
            }
        }

#ifdef CHECK_GAMMAS
        ftemp2 = 0.0;
#endif // CHECK_GAMMAS

        for(i = 0; i < N; ++i)
        {
            step[t].gamma[i] = 0.0;
            for(j = 0; j < N; ++j)
            {
                step[t].diGamma[i][j] = (step[t].alpha[i] * A[i][j] * B[j][step[t + 1].obs] * step[t + 1].beta[j])
                                        / denom;
                step[t].gamma[i] += step[t].diGamma[i][j];
            }

#ifdef CHECK_GAMMAS
            // verify that gamma[i] == alpha[i]*beta[i] / sum(alpha[j]*beta[j])
            ftemp2 += step[t].gamma[i];
            ftemp = 0.0;
            for(j = 0; j < N; ++j)
            {
                ftemp += step[t].alpha[j] * step[t].beta[j];
            }
            ftemp = (step[t].alpha[i] * step[t].beta[i]) / ftemp;
            if(DABS(ftemp - step[t].gamma[i]) > EPSILON)
            {
                printf("gamma[%d] = %f (%f) ", i, step[t].gamma[i], ftemp);
                printf("********** Error !!!\n");
            }
#endif // CHECK_GAMMAS

        }// next i

#ifdef CHECK_GAMMAS
        if(DABS(1.0 - ftemp2) > EPSILON)
        {
            printf("sum of gamma's = %f (should sum to 1.0)\n", ftemp2);
        }
#endif // CHECK_GAMMAS

    }// next t

}// end computeGammas


//
// reestimate pi, the initial distribution
//
void reestimatePi(struct stepStruct *step,
                  double *piBar,
                  int N)
{
    int i;

    // reestimate pi[]
    for(i = 0; i < N; ++i)
    {
        piBar[i] = step[0].gamma[i];
    }

}// end reestimatePi


//
// reestimate the A matrix
//
void reestimateA(struct stepStruct *step,
                 double **Abar,
                 int N,
                 int T)
{
    int i,
        j,
        t;

    double numer,
           denom;

    // reestimate A[][]
    for(i = 0; i < N; ++i)
    {
        for(j = 0; j < N; ++j)
        {
            numer = denom = 0.0;

            // t = 0,1,2,...,T-1
            for(t = 0; t < T - 1; ++t)
            {
                numer += step[t].diGamma[i][j];
                denom += step[t].gamma[i];

            }// next t

            Abar[i][j] = numer / denom;

        }// next j

    }// next i

} // end reestimateA


//
// reestimate the B matrix
//
void reestimateB(struct stepStruct *step,
                 double **Bbar,
                 int N,
                 int M,
                 int T)
{
    int i,
        j,
        t;

    double numer,
           denom;

    // reestimate B[][]
    for(i = 0; i < N; ++i)
    {
        for(j = 0; j < M; ++j)
        {
            numer = denom = 0.0;

            // t = 0,1,2,...,T-1
            for(t = 0; t < T - 1; ++t)
            {
                if(step[t].obs == j)
                {
                    numer += step[t].gamma[i];
                }
                denom += step[t].gamma[i];

            }// next t

            Bbar[i][j] = numer / denom;

        }// next j

    }// next i

}// end reestimateB


//
// initialize pi[], A[][] and B[][]
//
void initMatrices(double *pi,
                  double **A,
                  double **B,
                  int N,
                  int M,
                  int seed)
{
    int i,
        j;

    double prob,
           ftemp,
           ftemp2;

    // initialize pseudo-random number generator
    srand(seed);

    // initialize pi
    prob = 1.0 / (double)N;
    ftemp = prob / 10.0;
    ftemp2 = 0.0;
    for(i = 0; i < N; ++i)
    {
        if((rand() & 0x1) == 0)
        {
            pi[i] = prob + (double)(rand() & 0x7) / 8.0 * ftemp;
        }
        else
        {
            pi[i] = prob - (double)(rand() & 0x7) / 8.0 * ftemp;
        }
        ftemp2 += pi[i];

    }// next i

    for(i = 0; i < N; ++i)
    {
        pi[i] /= ftemp2;
    }

    // initialize A[][]
    prob = 1.0 / (double)N;
    ftemp = prob / 10.0;
    for(i = 0; i < N; ++i)
    {
        ftemp2 = 0.0;
        for(j = 0; j < N; ++j)
        {
            if((rand() & 0x1) == 0)
            {
                A[i][j] = prob + (double)(rand() & 0x7) / 8.0 * ftemp;
            }
            else
            {
                A[i][j] = prob - (double)(rand() & 0x7) / 8.0 * ftemp;
            }
            ftemp2 += A[i][j];

        }// next j

        for(j = 0; j < N; ++j)
        {
            A[i][j] /= ftemp2;
        }

    }// next i

    // initialize B[][]
    prob = 1.0 / (double)M;
    ftemp = prob / 10.0;
    for(i = 0; i < N; ++i)
    {
        ftemp2 = 0.0;
        for(j = 0; j < M; ++j)
        {
            if((rand() & 0x1) == 0)
            {
                B[i][j] = prob + (double)(rand() & 0x7) / 8.0 * ftemp;
            }
            else
            {
                B[i][j] = prob - (double)(rand() & 0x7) / 8.0 * ftemp;
            }
            ftemp2 += B[i][j];

        }// next j

        for(j = 0; j < M; ++j)
        {
            B[i][j] /= ftemp2;
        }

    }// next i

}// end initMatrices


//
// print pi[]
//
void printPi(double *pi, int N)
{
    int i;

    double ftemp;

    ftemp = 0.0;
    for(i = 0; i < N; ++i)
    {
        printf("%8.5f ", pi[i]);
        ftemp += pi[i];
    }
    printf(",  sum = %f\n", ftemp);

}// end printPi


//
// print A[][]
//
void printA(double **A, int N)
{
    int i,
        j;

    double ftemp;

    for(i = 0; i < N; ++i)
    {
        ftemp = 0.0;
        for(j = 0; j < N; ++j)
        {
            printf("%8.5f ", A[i][j]);
            ftemp += A[i][j];
        }
        printf(",  sum = %f\n", ftemp);

    }// next i

}// end printA


//
// print BT[][]
//
void printBT(double **B, int N, int M, char **alphabet)
{
    int i,
        j;

    double ftemp;

//    char alphabet[M] = ALPHABET;

    for(i = 0; i < M; ++i)
    {
        printf("%s\t", alphabet[i]);

        for(j = 0; j < N; ++j)
        {
            printf("%8.5f ", B[j][i]);
        }
        printf("\n");
    }
    for(i = 0; i < N; ++i)
    {
        ftemp = 0.0;
        for(j = 0; j < M; ++j)
        {
            ftemp += B[i][j];
        }
        printf("sum[%d] = %f ", i, ftemp);
    }
    printf("\n");

}// end printB


//
//  write model (pi, A, B) to file
//
void writeModel(double *pi, 
                double **A, 
                double **B, 
                int N, 
                int M, 
                int T, 
                char **alphabet,
                FILE *out)
{
    fprintf(out, "N=%d, M=%d, T=%d\n", N, M, T);
    fprintf(out, "I:\n");
    writePi(pi, N, out);
    fprintf(out, "A:\n");
    writeA(A, N, out);
    fprintf(out, "B:\n");
    writeBT(B, N, M, alphabet, out);
}


//
// write pi[] to file 
//
void writePi(double *pi, int N, FILE *out)
{
    int i;

    for(i = 0; i < N; ++i)
    {
        fprintf(out, "%.14f\t", pi[i]);
    }
    fprintf(out, "\n");

}// end printPi


//
// write A[][] to file
//
void writeA(double **A, int N, FILE *out)
{
    int i,
        j;

    for(i = 0; i < N; ++i)
    {
        for(j = 0; j < N; ++j)
        {
            fprintf(out, "%.14f\t", A[i][j]);
        }
        fprintf(out, "\n");
    }

}// end printA


//
// write BT[][] to file
//
void writeBT(double **B, int N, int M, char **alphabet, FILE *out)
{
    int i,
        j;

    for(i = 0; i < M; ++i)
    {
        fprintf(out, "%s\t", alphabet[i]);

        for(j = 0; j < N; ++j)
        {
            fprintf(out, "%.14f\t", B[j][i]);
        }
        fprintf(out, "\n");
    }

}// end printB

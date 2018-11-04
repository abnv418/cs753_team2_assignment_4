# cs753_team2_assignment_4


## Installation

Clone the project repository and change into it.

```bash
git clone https://github.com/abnv418/cs753_team2_assignment_4.git
cd cs753_team2_assignment4
```

Have Maven installed. Use the compile script to compile the project jar (will be located in target/)

```bash
./compile.sh
```

## Indexing

A paragraph index must first be created before searching can be done. Use the index command to create the directory:

```bash
java -jar target/team2_4-1.0-SNAPSHOT-jar-with-dependencies.jar index CBOR
```

Where CBOR is the location of the paragraphs cbor file to be used with indexing. This will create the *paragraphs/* directory in the project directory.


## Searching

There are four search variants in this project. Each one will creature a runfile (.run), and print the top paragraph return for the Brush rabbit query (which we discuss in the report).
Each search command is formatted in a similar way. To run one, do:

```bash
java -jar target/team2_4-1.0-SNAPSHOT-jar-with-dependencies.jar SEARCH paragraphs/ OUTLINES
```
Where paragraphs/ is the index created by the index command (make sure you point to the correct path), OUTLINES is the cbor outlines file that is used in querying, and SEARCH is the name of the search variant to use (see below).

 * **searchLaplace**: Searches using the Laplace variant. Output: laplace_run.run
 * **searchJM**: Searches using the Jelenik-Mercer variant. Output: jm.run
 * **searchDirichlet**: Searches using the Dirichlet variant. Output: dr_run.run
 * **searchBigram**: Searrches using the Bigram variant (and with Laplace smoothing). Output: bigram_run.run

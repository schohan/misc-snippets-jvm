-> Youtube Data Ingestion Strategy:

1. Download data by utilizing keywords based search. Keywords are supplied from directory of topic files configured in application.properties.
2. Save raw json as is in a given directory with file names representing the query.
3. Extract data fields of interest from raw data (these would be indexed/stored in elasticsearch) and store then in a 'processed'
director under the source directory. This is done to keep data close to each other.
4. Store processed data in ElasticSearch

-> First time load
Same as Updates

-> Updates
1. Download new data based on configured frequency utilizing the keywords from a source file/database
2. Check data Ids to filter new results
3. Follow steps 3 and 4. from Ingestion Strategy.

-> Deletes
1. A job checks validity of stored content and delete invalid content based on expired date




# ADV PROJECT
  - download the "executable" to use the application
  - finished 5/10/2023 //ddmmyy
    - maybe i should add loading screen?
# Team member (for submission)
  652115001  Karanthaphong Areerak​
  652115002  Krittayotch Kheunchan​
  652115013  Narongchai Rongthong
# HOW TO USE
  - upload files in grey box with file icon 
    (files only accepts png/jpg/zip)
  - choose to watermark/ resize
  - customize as desired
  - select output type (png/jpg)
  - click confirm and select path
# codes info
  - multithread testing is in the watermark view,
    uncomment // test = true // to start testing the speed different (ms)

                  @FXML
                  public void handleConfirmation() {
                      ...
                      executorService.shutdown();
                      // wait for the process
                      try {
                          executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      }
                      ...
                  //  testing 1 thread for time different
                  Boolean test = false;
                  //test = true; < - UNCOMMENT THIS --------------------------------------------------------------------
                  if (test) {...} // test close

# end of readme file

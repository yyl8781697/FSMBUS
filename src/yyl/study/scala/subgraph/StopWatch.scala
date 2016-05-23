package yyl.study.scala.subgraph

class StopWatch {
	private var startTime = 0L;
    private var stopTime = 0L;
    private var running = false;

    
    def start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    
    def stop() {
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }

    
    //elaspsed time in milliseconds
    def getElapsedTime():Long= {
        var elapsed=0L;
        if (running) {
             elapsed = (System.currentTimeMillis() - startTime);
        }
        else {
            elapsed = (stopTime - startTime);
        }
        
        elapsed;
    }
    
    
    //elaspsed time in seconds
    def getElapsedTimeSecs():Long= {
        var elapsed=0L;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 1000);
        }
        else {
            elapsed = ((stopTime - startTime) / 1000);
        }
        
        elapsed;
    }
}
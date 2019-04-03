package MergeSortTwoPhase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;





public class TwoPhaseMergeSort {
	static final int recordSize = 4;
	static String outputFile="output";
	static  Integer totalNumberOfRecords =0;
	static  Integer mainMemeoryRecords =0;
	static  Integer numberOfSubLists =0 ;
	static  Integer blockSize =0;
	static  Integer numberOfMainMemoryBuffers=0;
	static  Integer numberOfInputBuffers=0;
	static  Integer numberOfPages=(1000000*4)/(1024);
	static Integer numberOfPasses=0;
	
	public static void main(String[] args) throws Exception {
		FileInputStream  fileInputStream = new FileInputStream(new File("C:\\Sandeep\\Workspaces\\ProjectWorkSpace\\FinalLabProj1\\input1.txt"));
		BufferedInputStream bin=new BufferedInputStream(fileInputStream); 
		Scan buff = new Scan(bin);
		totalNumberOfRecords=buff.nextInt();
		 String memory=	buff.next();
		 long free = Runtime.getRuntime().freeMemory()/2;
		 mainMemeoryRecords = (int) ((free)/((recordSize)));
		 numberOfSubLists = (int) Math.ceil(((double)totalNumberOfRecords) / ((double)(mainMemeoryRecords))) ;
		 //blockSize =  mainMemeoryRecords/(numberOfSubLists+1);
		 blockSize = 1024*8;
		/* if(blockSize<=0) {
			 blockSize = 1;
		 }*/
		 numberOfMainMemoryBuffers = (int) ((free)/(blockSize*4) );
		 numberOfInputBuffers = numberOfMainMemoryBuffers - 1;
		 numberOfPages=(1000000*4)/(1024);
		 numberOfPasses = (int) Math.ceil( (Math.log10(numberOfSubLists) / Math.log10(numberOfInputBuffers)) );
		 long beginTime = System.nanoTime();
		try {
			System.out.println("Phase 1 has started");
			phaseOne(buff);
			fileInputStream.close();
			System.out.println(numberOfSubLists+" Sublists are created in the first phase.");
			phaseTwoAndOnwards();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			long endTime = System.nanoTime();
			System.out.println("Time taken :"+((endTime-beginTime)/1000000000.0));
	}
	
	public static void phaseOne(Scan buff)
	{
	try {
		int count = 0;
		int size=0;
		int subListNum=0;
		int[] recordsList;
		int records =mainMemeoryRecords;
		while(size!=totalNumberOfRecords)
		{
			if(numberOfSubLists == 1 && totalNumberOfRecords<mainMemeoryRecords) {
				recordsList = new int[totalNumberOfRecords];
				records = totalNumberOfRecords;
			}
			else {
			if(mainMemeoryRecords<(totalNumberOfRecords-size)) {
				recordsList = new int[mainMemeoryRecords];
			}
			else {
				recordsList = new int[totalNumberOfRecords-size];
			}
			}
			int j;
	            while(count!=records)
	            {
	            	j=buff.nextInt();
	            	if(j!=-1) {
	            		 recordsList[count]=j;
	            		 count++;
	            	}
	            	else break;
	            }
			Arrays.sort(recordsList);
			createFile("Output1"+subListNum+".txt");
			writeToFile("Output1"+subListNum+".txt", recordsList);
			size = size+count;
			count=0;
			subListNum++;
		}
	} catch (Exception e) {
		e.printStackTrace();
	}     
	
}

	public static void phaseTwoAndOnwards() throws Exception
	{
		System.gc();
		try {
			int i=0;
			long freemem=0;
			if(numberOfSubLists==1) //only single output file
			{
				File oldName = new File("Output"+"10.txt");
			    File newName = new File(outputFile);
			    oldName.renameTo(newName);
			    return;
			}
			
			for (int passCount = 1; passCount <= numberOfPasses; passCount++)
			{
				int initialSublists = 0;
				int initial =0 ;
				int outputLen=0;
				int counter=0;
				int numGroups = (int) Math.ceil((double) numberOfSubLists / (double) numberOfInputBuffers);
				int numSub=numberOfSubLists;
				for (int groupCount = 0; groupCount < numGroups; groupCount++)
				{
					int total = 0;
					Vector<FileList> readF = new  Vector<>();
					//if(groupCount>0)
					if((groupCount+1)==numGroups) {
						total=numSub;
					}
					else {
					if(numberOfSubLists>numberOfInputBuffers) {
					 total = numberOfInputBuffers;
					}
					}
					if(numberOfSubLists>numberOfInputBuffers) {
						
						numSub=numSub-numberOfInputBuffers;
					}
					
					for(i=0;i<total;i++)
					{
						
						try {
							FileList rd = new FileList();
							  rd.fileInputStream = new BufferedInputStream(new FileInputStream(new File("Output"+passCount+counter+".txt")));
							  counter++;
							  if(rd.fileInputStream==null) {
								  break;
							  }
							Scan buff = new Scan(rd.fileInputStream);
							rd.buff = buff;
							readF.add(rd);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println(freemem);
							e.printStackTrace();
						}
					}
					
					String outputGroupFile;
					if(numGroups!=1) {
					 outputGroupFile = "Output"+(passCount+1)+groupCount+".txt";
					} 
					else {
						 outputGroupFile = "FinalOutput"+(passCount+1)+groupCount+".txt";
					}
					int j;
					 initial=initialSublists;
						for(int k=0;k<(total);k++) {
							initialSublists++;
							readF.get(k).tab= new int[blockSize];
						for(int u=0;u<blockSize;u++) {
						//	a = new ArrayList<>();
							if(readF.get(k)!=null)
								try {
									j=readF.get(k).buff.nextInt();
									if(j!=-1)
										readF.get(k).tab[(readF.get(k).len)++]=j;
								}catch (Exception e) {
									System.out.println(Runtime.getRuntime().freeMemory());
									e.printStackTrace();
								}
							
						}
						if(readF.get(k).len!=0 && readF.get(k).len!=blockSize) {
							while(readF.get(k).len!=blockSize) {
								readF.get(k).tab[(readF.get(k).len)++]=-1;
							}
						}
					}
						
						
			int minmax=0;
			int[] output = new int[blockSize]; 
			outputLen=0;
			while(true)
			{
				List<Integer> tempTab = new ArrayList<>();
				int count = 0;
				for(i=(0);i<(total);i++)
				{
					if(readF.get(i)==null || readF.get(i).tab==null)
					{
						tempTab.add(null);
						count++;
						continue;
					}
					if(readF.get(i).l >= (readF.get(i).tab.length))
					{
						readF.get(i).tab= new int[blockSize];
						readF.get(i).l=0;
						readF.get(i).len=0;
						int p;
						
					for(int u=0;u<blockSize;u++) {
						if(readF.get(i)!=null && readF.get(i).buff!=null)
							try {
								p=readF.get(i).buff.nextInt();
								if(p!=-1)
									readF.get(i).tab[(readF.get(i).len)++]=p;
								else 
									break;
							}catch (Exception e) {
								System.out.println(Runtime.getRuntime().freeMemory());
								e.printStackTrace();
							}
					}
					if(readF.get(i).len!=0 && readF.get(i).len!=blockSize) {
						while(readF.get(i).len!=blockSize) {
							readF.get(i).tab[(readF.get(i).len)++]=-1;
						}
					}
					
					if(readF.get(i).len==0)
					{
							readF.get(i).fileInputStream.close();
							readF.get(i).buff=null;
							readF.get(i).tab=null;
							tempTab.add(null);
							count++;
					}
				else if(readF.get(i).tab[(readF.get(i).l)]==-1){
					readF.get(i).fileInputStream.close();
					readF.get(i).buff=null;
					readF.get(i).tab=null;
					tempTab.add(null);
					count++;
				}
				else
				{
					if(readF.get(i).tab[(readF.get(i).l)]!=-1)
					tempTab.add(readF.get(i).tab[(readF.get(i).l)]);
				}
					}
					else if(readF.get(i).tab[(readF.get(i).l)]==-1){
						readF.get(i).fileInputStream.close();
						readF.get(i).buff=null;
						readF.get(i).tab=null;
						tempTab.add(null);
						count++;
					}
					else
					{
						if(readF.get(i).tab[(readF.get(i).l)]!=-1)
						tempTab.add(readF.get(i).tab[(readF.get(i).l)]);
					}
				}
				
				if(count == (total)) // all buffers have become null
				{
					writeToTheOutputFile(outputGroupFile, output,outputLen);
					break;
				}
				
					minmax = getMin(tempTab);
				
				output[outputLen++]=(readF.get(minmax).tab[(readF.get(minmax).l)++]);
				if(outputLen>=(blockSize))
				{
					writeToTheOutputFile(outputGroupFile, output,outputLen);
					output = new int[blockSize];
					outputLen=0;
				}
				
				
			}
			}
			numberOfSubLists = (int) Math.ceil((double) numberOfSubLists / (double) (numberOfInputBuffers)) ;
			}
		}catch (Exception e) {
			System.out.println(Runtime.getRuntime().freeMemory());
			System.out.println("***************EXCEPTION******************");
			e.printStackTrace();
		}		
	}
	
	public static int getMin(List<Integer> tab)
	{
		List<Integer> sort = new ArrayList<>();
		int minIndex = 0;
		for(int i=0;i<tab.size();i++) {
			if(tab.get(i)!=null && tab.get(i)!=-1) {
				sort.add(tab.get(i));
			}
		}
		if(tab!=null && sort!=null) {
			try {
				minIndex = tab.indexOf(Collections.min(sort));
			}
		 catch (Exception e) {
			 System.out.println("444");
			 for(int i=0;i<tab.size();i++) {
					if(tab.get(i)!=null) {
						System.out.println(tab.get(i));
					}
				}
			 for(int i=0;i<sort.size();i++) {
					
						System.out.println(sort.get(i));
					}
				
			 e.printStackTrace();
		}
		}
		return minIndex;
	}
	
	public static void writeToFile(String fileName, int[] tab ) throws IOException
	{
		FileWriter pw = null;
	    int size=tab.length;
		try {
				
			pw = new FileWriter(fileName,true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			for(int j=0;j<tab.length;j++)
			{
				pw.write(String.valueOf(tab[j]));
				pw.write("\n");
			}
	        pw.close();
	}
	
	public static void writeToTheOutputFile(String fileName, int[] tab,int leng ) throws IOException
	{
		FileWriter pw = null;
			try {
				
				pw = new FileWriter(fileName,true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int j=0;j<leng;j++)
			{
				pw.write(String.valueOf(tab[j]));
				pw.write("\n");
			}
	        pw.close();
	}
	
	
	public static void writeToTheOutputFile(String fileName, List<Integer> tab )
	{
		PrintWriter pw = null;
		for(int i = 0;i<tab.size();i++)
		{
			StringBuilder sb = new StringBuilder();
			List<Integer> tuple = tab;
			sb.append(tuple.get(i).toString());
			sb.append(" ");
			sb.append("\n");
				try {
					
					pw = new PrintWriter(new FileWriter(fileName,true));
				} catch (IOException e) {
					e.printStackTrace();
				}
				pw.write(sb.toString());
		        pw.close();
		}
	}
	
	
	public static void createFile(String fileName)
	{
		PrintWriter pw = null;
		
		try {
			
			pw = new PrintWriter(new FileWriter(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
        pw.close();
	}

	
}


 class FileList {
	public BufferedInputStream fileInputStream;
	public int l=0;
	public int len=0;
	public Scan buff;
	public int[] tab;
}



 

  class Scan {
    private BufferedInputStream in;

    int c;
    
    
    boolean atBeginningOfLine;

    public Scan(InputStream stream) {
       in = new BufferedInputStream(stream);
       try {
          atBeginningOfLine = true;
          c  = (char)in.read();
       } catch (IOException e) {
          c  = -1;
       }
    }

    public boolean hasNext() {
       if (!atBeginningOfLine) 
          throw new Error("hasNext only works "+
          "after a call to nextLine");
       return c != -1;
    }

    public String next() {
    	StringBuffer sb = new StringBuffer();
       atBeginningOfLine = false;
       try {
          while (c <= ' ' && c!=-1) {
             c = in.read();
          } 
          while (c > ' ') {
             sb.append((char)c);
             c = in.read();
          }
       } catch (IOException e) {
          c = -1;
          return "";
       }
       return sb.toString();
    }

    public String nextLine() {
       StringBuffer sb = new StringBuffer();
       atBeginningOfLine = true;
       try {
          while (c != '\n') {
             sb.append((char)c);
             c = in.read();
          }
          c = in.read();
       } catch (IOException e) {
          c = -1;
          return "";
       }
       return sb.toString();   
    }

    public int nextInt() {
       String s = next();
       try {
    	   if(!s.isEmpty()) {
          return Integer.parseInt(s);}
    	   else {
    		   in.close();
    		   return -1;
    	   }
       } catch (Exception e) {
    	   System.out.println("Emptyyyyyyyyy");
          return -1; //throw new Error("Malformed number " + s);
       }
    }

    public double nextDouble() {
       return new Double(next());
    }

    public long nextLong() {
       return Long.parseLong(next());
    } 

    public void useLocale(int l) {}
 }


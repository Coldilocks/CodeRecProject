package utils;

import java.util.HashSet;
import java.util.Set;

public class PackageElement {
	private String packageString;
	private Set<String> packageNames;
	private String className;
	
	public String getpackageString() {
		return packageString;
	}
	
	public String getPackageName(){
		String[] packageArray = packageString.split(" ");
		return packageArray[packageArray.length-1];
	}
	
	public boolean setPackageString(String packageString) {
		if(!packageString.contains(";") || !packageString.contains(" ")){
			return false;
		}
		packageString = packageString.substring(0, packageString.length()-1);
		this.packageString = packageString;
		this.splitpackageString();
		return true;
	}
	
	private void splitpackageString(){
		String[] packageNameArray = packageString.substring(packageString.indexOf(" ")+1, packageString.indexOf(";")).split("\\.");				
		packageNames = new HashSet<String>();
		for(String packageName : packageNameArray){
			packageNames.add(packageName);
		}
		
		className = packageNameArray[packageNameArray.length-1];
	}
	
	public Set<String> getpackageNames(){
		return packageNames;
	}
	
	public boolean matchpackageNames(PackageElement packageElement){
		Set<String> anotherPackageNames = packageElement.getpackageNames();
		Set<String> result = new HashSet<String>();
		result.addAll(anotherPackageNames);
		result.addAll(packageNames);
		int totalNumber = result.size();
		
		result.clear();
		result.addAll(anotherPackageNames);
		result.retainAll(packageNames);
		int coOccurNumber = result.size();
		
		double rate = (coOccurNumber * 1.0) / totalNumber;
		if(rate >= 0.5){
			return true;
		}
		return false;
	}
	
	public String matchClassName(String sentence){
		sentence = sentence.replace(';', ' ');
		String[] items = sentence.split(" ");
		
		for(int i=0; i<items.length-1; i++){		
			String item = items[i];
			String className = this.className.toLowerCase();
			if(className.equals(item.toLowerCase())){
				return items[i+1];
			}
			
		}
		
		return null;
	}
	
	public boolean isClassThisName(String className){
		return this.className.toLowerCase().equals(className.toLowerCase());
	}
	
	public String getClassName(){
		return this.className;
	}
}

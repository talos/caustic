#!/bin/sh

URL="http://localhost:4567"

echo 
curl -X POST $URL/namespace/C36047
echo
curl -X POST $URL/namespace/NY.NYC.Brooklyn
echo
curl -X POST $URL/namespace/NY.NYC
echo
curl -X POST $URL/namespace/C36047/parent/NY.NYC.Brooklyn
echo
curl -X POST $URL/namespace/NY.NYC.Brooklyn/parent/NY.NYC
echo
curl -X POST $URL/type/Property
echo
curl -X POST $URL/type/Property/publish/streetNum
echo
curl -X POST $URL/type/Property/publish/streetDir
echo
curl -X POST $URL/type/Property/publish/streetName
echo
curl -X POST $URL/type/Property/publish/streetSuffix
echo
curl -X POST $URL/type/Property/publish/city
echo
curl -X POST $URL/type/Property/publish/zip
echo
curl -X POST $URL/type/Party
echo
curl -X POST $URL/type/Party/publish/name
echo
curl -X POST $URL/type/Party/publish/address
echo
curl -X POST $URL/information/NY.NYC.Brooklyn/Property
echo
curl -X POST -d "value=3" $URL/information/NY.NYC.Brooklyn/Property/default/boroughNumber
echo

echo
curl -X POST $URL/gatherer/NY.NYC.BIS.Profile
echo
curl -X POST -d "value=http://a810-bisweb.nyc.gov/bisweb/PropertyProfileOverviewServlet" $URL/gatherer/NY.NYC.BIS.Profile/url
echo
curl -X POST $URL/gatherer/NY.NYC.DOF.PropertyAddressSearch
echo
curl -X POST -d "value=http://webapps.nyc.gov:8084/CICS/FIN1/FIND001I" $URL/gatherer/NY.NYC.DOF.PropertyAddressSearch/url
echo
curl -X POST $URL/gatherer/NY.NYC.ACRIS.Parcel.All
echo
curl -X POST -d "value=http://a836-acris.nyc.gov/Scripts/DocSearch.dll/BBLResult"  $URL/gatherer/NY.NYC.ACRIS.Parcel.All/url
echo
curl -X POST $URL/gatherer/NY.NYC.ACRIS.Parcel.Deeds
echo
curl -X POST $URL/gatherer/NY.NYC.ACRIS.Parcel.Deeds/parent/NY.NYC.ACRIS.Parcel.All
echo
curl -X POST $URL/gatherer/NY.NYC.ACRIS.Parcel.Mortgages
echo
curl -X POST $URL/gatherer/NY.NYC.ACRIS.Parcel.Mortgages/parent/NY.NYC.ACRIS.Parcel.All
echo
curl -X POST -d "value=$R{boroughNumber}" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_borough
echo
curl -X POST -d "value=$R{block}" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_block
echo
curl -X POST -d "value=$R{lot}" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_lot
echo
curl -X POST -d "value=http://a836-acris.nyc.gov/Scripts/DocSearch.dll/BBL" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/header/Referer
echo
curl -X POST -d "value=50" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/get/max_rows
echo
curl -X POST -d "value=To Current Date" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_selectdate
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_datefromm
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_datefromd
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_datefromy
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_datetom
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_datetod
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_datetoy
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_doctype_name
echo
curl -X POST -d "value=50" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_max_rows
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_page
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_ReqID
echo
curl -X POST -d "value=N" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_ISIntranet
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_EmployeeID
echo
curl -X POST -d "value=BBL" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_SearchType
echo
curl -X POST -d "value=YES" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/cookie/JUMPPAGE
echo
curl -X POST -d "value=MORTGAGES & INSTRUMENTS" $URL/gatherer/NY.NYC.ACRIS.Parcel.Mortgages/post/hid_doctype_name
echo
curl -X POST -d "value=ALL_MORT" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_doctype
echo
curl -X POST -d "value=DEEDS AND OTHER CONVEYANCES" $URL/gatherer/NY.NYC.ACRIS.Parcel.Mortgages/post/hid_doctype_name
echo
curl -X POST -d "value=ALL_DEED" $URL/gatherer/NY.NYC.ACRIS.Parcel.All/post/hid_doctype
echo
curl -X POST -d "value=http://a810-bisweb.nyc.gov/bisweb/bispi00.jsp" $URL/gatherer/NY.NYC.BIS.Profile/header/Referer
echo
curl -X POST -d "value= GO " $URL/gatherer/NY.NYC.BIS.Profile/get/go2
echo
curl -X POST -d "value=0" $URL/gatherer/NY.NYC.BIS.Profile/get/requestid
echo
curl -X POST -d "value=$R{boroughNumber}" $URL/gatherer/NY.NYC.BIS.Profile/get/boro
echo
curl -X POST -d "value=$R{streetNumber}" $URL/gatherer/NY.NYC.BIS.Profile/get/houseno
echo
curl -X POST -d "value=$R{streetDir} $R{streetName} $R{streetSuffix}" $URL/gatherer/NY.NYC.BIS.Profile/get/street
echo
curl -X POST -d "value=http://webapps.nyc.gov:8084/CICS/fin1/find001I" $URL/gatherer/NY.NYC.DOF.PropertyAddressSearch/header/Referer
echo
curl -X POST -d "value=" $URL/gatherer/NY.NYC.DOF.PropertyAddressSearch/post/FAPTNUM
echo
curl -X POST -d "value=A" $URL/gatherer/NY.NYC.DOF.PropertyAddressSearch/post/FFUNC
echo
curl -X POST -d "value=SEARCH" $URL/gatherer/NY.NYC.DOF.PropertyAddressSearch/post/DFH_ENTER
echo
curl -X POST -d "value=$R{boroughNumber}" $URL/gatherer/NY.NYC.DOF.PropertyAddressSearch/post/FBORO
echo
curl -X POST -d "value=$R{streetNumber}" $URL/gatherer/NY.NYC.DOF.PropertyAddressSearch/post/FHOUSENUM
echo
curl -X POST -d "value=$R{streetDir} $R{streetName} $R{streetSuffix}" $URL/gatherer/NY.NYC.DOF.PropertyAddressSearch/post/FSTNAME
echo

echo
curl -X POST $URL/information/NY.NYC/Property/gatherer/NY.NYC.BIS.Profile
echo
curl -X POST $URL/information/NY.NYC/Property/gatherer/NY.NYC.DOF.PropertyAddressSearch
echo
curl -X POST $URL/information/NY.NYC/Property/gatherer/NY.NYC.ACRIS.Parcel.Deeds
echo
curl -X POST $URL/information/NY.NYC/Property/gatherer/NY.NYC.ACRIS.Parcel.Mortgages
echo

echo
curl -X POST -d "regex=<!--Table Begin!-->([\s\S]+?)<!--Table End-->" $URL/information/NY.NYC/Property/gatherer.NY.NYC.ACRIS.Parcel.Deeds/0/to/ACRIS.Parcel.Deeds.table
echo
curl -X POST -d "regex=<!--Table Begin!-->([\s\S]+?)<!--Table End-->" $URL/information/NY.NYC/Property/gatherer.NY.NYC.ACRIS.Parcel.Mortgages/0/to/ACRIS.Parcel.Mortgages.table
echo
curl -X POST -d "regex=BIN#[^\d*](\d+)" $URL/information/NY.NYC/Property/gatherer.NY.NYC.BIS.Profile/0/to/BIN
echo
curl -X POST -d "regex=BIN#[^\d*](\d+)" $URL/information/NY.NYC/Property/gatherer.NY.NYC.BIS.Profile/0/to/BIN
echo
curl -X POST -d "regex=<td class=\"maininfo\"[^>]*>[^<]*</td>[^<]*<td class=\"maininfo\"[^>]*>([^0-9]+)" $URL/information/NY.NYC/Property/gatherer.NY.NYC.BIS.Profile/0/to/BIS.city
echo
curl -X POST -d "<input\s+type=\"hidden\"\s+name=\"q49_block_id\"\s+value=\"([^\"]+)\">" $URL/information/NY.NYC/Property/gatherer.NY.NYC.DOF.PropertyAddressSearch/0/to/block
echo
curl -X POST -d "<input\s+type=\"hidden\"\s+name=\"q49_lot\"\s+value=\"([^\"]+)\">" $URL/information/NY.NYC/Property/gatherer.NY.NYC.DOF.PropertyAddressSearch/0/to/lot
echo

echo
curl -X POST -d "<input\s+type=\"hidden\"\s+name=\"ownerName\d{0,1}\"\s+value=\"\s*([^\"]+)\">" $URL/information/NY.NYC/Property/gatherer.NY.NYC.DOF.PropertyAddressSearch/to/NY.NYC/Owner/name
echo
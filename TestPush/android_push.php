<?php

// Set POST variables
$url = 'https://android.googleapis.com/gcm/send';

//////////////////////////////

//The smartphone TOKEN
$TOKEN="..."; //<-----------------------------------------------------

//La clef de l'application que l'on vient de générer avec les identifiants sur la console Google / server key
$API_KEY = 'AIzaSyAtJDOTVLArDl3KZ7lOwGffSzk6pjdv2VU';

$MESSAGE = "Test Notification";
$TITRE = "Title of the notification";

$datas = array( "titre" => $TITRE , "message" => $MESSAGE);

if(isset($IMAGE))
	$datas["image"] = $IMAGE;

if(isset($ID))
	$datas["id"] = $ID;

$fields = array(

				'registration_ids'  => array( $TOKEN ),
			    'data'              => $datas
                );

$headers = array( 
					'Authorization: key=' . $API_KEY,
                    'Content-Type: application/json'
                );

// Open connection
$ch = curl_init();


// Set the url, number of POST vars, POST data
curl_setopt( $ch, CURLOPT_URL, $url );

curl_setopt( $ch, CURLOPT_POST, true );
curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1 );

curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);    // 2 is the default so this is not required


curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode( $fields ) );

// Execute post
$result = curl_exec($ch);

// Close connection
curl_close($ch);

//echo $result;


?>
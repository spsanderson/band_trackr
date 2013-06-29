========
Services
========

Janrain
=======
`Janrain <http://www.janrain.com>`_ user management platform for the social web is integrated in BandTrackr for login as well as for sharing.

Following login providers are enabled:

.. hlist::
   :columns: 3

   * Facebook
   * Twitter
   * LinkedIn
   * Google
   * Yahoo
   * OpenID

and here is a list of currently enabled sharing providers:

.. hlist::
   :columns: 2

   * Facebook
   * Twitter
   * LinkedIn
   * Yahoo

Beside those, info on events can also be shared via e-mail or SMS.

.. _event-services-label:

Event Services
==============
BandTrackr takes advantage of several Event Services available on the internet. You can choose your favorite service or let BandTrackr choose. At the moment Event Services are invoked sequentially, but there are plans to enhance and to implement algorithms that will track user behavior so we will be able to offer you best possible user experience.
Event search is being invoked in two cases, when fetching events for user current location, and for fetching events for user travel trip within the traveling period (see :ref:`tripit-label`). Event search is issued based on latitude and longitude retrieved either by phone's GPS/Network or location provided by :ref:`tripit-label`.

We have following Event Services integrated into BandTrackr:

.. hlist::
	:columns: 1
	
	* `TicketLeap <http://www.ticketleap.com/>`_
	* `Bandsintown <http://www.bandsintown.com>`_
	* `Eventful <http://eventful.com>`_

last.fm
=======
To further improve User experience we've allowed the user to enable `Last.fm <http://www.last.fm>`_. If this service is enabled, user's loved artists are bing pulled and cached based on which recommended events are being underlined.
This allows the user to quickly identify events of favorite bands and artists, read more info, buy tickets, etc..
Besides this `Last.fm <http://www.last.fm>`_ WIKI service is used as base for additional info on artists.

.. _tripit-label:

TripIt
======
If you like traveling and you have a `TripIt <http://www.tripit.com>`_ account (or want one) this feature is of interest for you. With `TripIt <http://www.tripit.com>`_ we enable you to lookup events and happenings right at your destination location and within the period you've planed to be there.

Foursquare
==========
While being at an event that you've noticed and bought tickets for via BandTrackr, now you can also check in to Foursquare directlly from within BandTrackr with your own Foursquare account.
By clicking on the Foursquare check in logo, location fetched on your phone will be sent and venues will be searched to which you can check in.
(comming soon: add venues to Foursquare from within BandTrackr)

.. _media-share-label:

Media share
============
With latest release we've provided you one more highlight feature - now you can share your photos and videos directly via the BandTrackr application.
The feature is fully integrated in your phone's camera and photo/video sharing service. This way you can quickly take photos and videos and share them via BandTrackr and let your friends know about the grate event you're at right now.

Flickr
======
As supporting service for :ref:`media-share-label`, we've choose to integrate `Flickr <http://www.flickr.com>`_. It enables the user to upload photos directly from the phone and to share it with the world.

YouTube
=======
As supporting service for :ref:`media-share-label`, we've choose to integrate `YouTube <http://www.youtube.com>`_. It enables the user to upload videos directly from the phone and to share it with the world.
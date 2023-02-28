INSERT INTO public.profile_image (id, image_name, image_role, image_url, upload_status, uploaded_at, user_id)
VALUES (1, 'image1.jpeg', 'UNSIGNED_IMAGE', 'https://fra1.digitaloceanspaces.com/kanwise/images/celders1/image1.jpeg',
        'FAILED', now() - interval '1 hour', 2),
       (2, 'image2.png', 'PROFILE_IMAGE', 'https://fra1.digitaloceanspaces.com/kanwise/images/celders1/image2.png',
        'SUCCESS', now(), 2);
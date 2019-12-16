# THERE IS A LOT OF NONSENSE IN HERE
# SCROLL TO THE BOTTOM FOR THE MAGIC MAFS



import numpy as np
import cv2
from cv2 import aruco
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D  # needed for 3d plot
import glob
import pickle
import matplotlib as mpl



COLOURS = ['#e6194B', '#f58231', '#ffe119', '#bfef45', '#3cb44b', '#42d4f4', '#4363d8',
           '#911eb4', '#f032e6', '#a9a9a9', '#800000', '#9A6324', '#808000', '#469990',
           '#000075', '#fabebe', '#ffd8b1', '#fffac8', '#aaffc3', '#e6beff', '#eeeeee',
           '#111111']





def read_vectors():
    with open('vectors.p', 'rb') as fp:
        vectors = pickle.load(fp)
    return vectors
vectors = read_vectors()



# def plot_gridboard_axes(image_path, vectors, factor, gridboards=None):
#     """
#     Used to plot multiple gridboards, with one large set of axes per gridboard
#     """
#     dist_coeffs = vectors[str(factor)]['dist_coeffs']
#     cam_matrix = vectors[str(factor)]['cam_matrix']
#     nx = 2
#     ny = 2
#     marker_size = 0.025 # 25mm
#     between_markers = 0.005 # 5mm
#     markers_per_board = nx * ny
#     length_of_axis = (marker_size + between_markers) * nx - between_markers
#     aruco_dict = aruco.Dictionary_get(aruco.DICT_4X4_250)
#     offset = 0
#     if not gridboards:
#         gridboards = [aruco.GridBoard_create(
#             nx, ny, marker_size, between_markers, aruco_dict, firstMarker=offset
#         )]
#     print(image_path)
#     frame = cv2.imread(image_path)
#     gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
#     corners, ids, rejectedImgPoints = aruco.detectMarkers(gray, aruco_dict)
#
#     all_rvecs = []
#     all_tvecs = []
#     # frame_with_markers = aruco.drawDetectedMarkers(frame.copy(), corners, ids)
#     frame_with_markers = frame.copy()
#
# #     print(f"ids={ids}")
#     for i, board in enumerate(gridboards):
#         # Go through all the corners / ids found in the image, and just select the
#         # ones relevant to the current board
#         ids_subset = ids.copy()
#         corners_subset = corners.copy()
#         for j, item in enumerate(ids):
#             if item[0] not in [val[0] for val in board.ids]:
#                 np.delete(ids_subset, j)
#                 np.delete(corners_subset, j)
#         # Reset the vectors
#         rvec = np.ones(shape=(3,1), dtype=float)
#         tvec = np.ones(shape=(3,1), dtype=float)
#         # Estimate the Board's pose
#         ret, rvec, tvec = aruco.estimatePoseBoard(
#                 corners_subset,
#                 ids_subset,
#                 board,
#                 cam_matrix,
#                 dist_coeffs,
#                 rvec,
#                 tvec
#         )
#
#         if ret == 0: # if no gridboard was found
#             print("Failed to get BoardPose")
#             rvecs, tvecs, objPoints = aruco.estimatePoseSingleMarkers(
#                                                     corners,
#                                                     marker_size,
#                                                     cam_matrix,
#                                                     dist_coeffs)
#             for i in range(len(tvecs)):
#                 frame_with_markers = cv2.drawFrameAxes(
#                                         frame_with_markers, cam_matrix,
#                                         dist_coeffs, rvecs[i], tvecs[i],
#                                         length_of_axis, thickness=2)
#
#         else:
#             frame_with_markers = cv2.drawFrameAxes(frame_with_markers, cam_matrix,
#                                                    dist_coeffs, rvec, tvec,
#                                                    length_of_axis, thickness=2)
#             all_rvecs.append(rvec)
#             all_tvecs.append(tvec)
#     plt.figure(figsize=(12,6))
#     plt.imshow(frame_with_markers, interpolation = "nearest")
#     return all_rvecs, all_tvecs


# Processing




# Variables
nx = 2
ny = 2
num_gridboards = 4
marker_size = 0.025 # 25mm
between_markers = 0.005
aruco_dict = aruco.Dictionary_get(aruco.DICT_4X4_250)
save_dir = "generated/gridboards"




def make_gridboards(nx, ny, marker_size, between_markers, aruco_dict, num_gridboards, save_dir):
    gridboards = []
    for i in range(num_gridboards):
        offset = i * nx * ny
        gridboards.append(aruco.GridBoard_create(nx, ny, marker_size, between_markers,
                                       aruco_dict, firstMarker=offset))
        if save_dir:
            imboard = gridboards[i].draw((500, 500))
            fig = plt.figure()
            ax = fig.add_subplot(1,1,1)
            title = f"{save_dir}/{nx}x{ny}_{offset}.png"
            plt.title(f"{title}\nmarker_size={marker_size}m, between_markers={between_markers}m")
            ax.axis("off")
            plt.imshow(imboard, cmap = mpl.cm.gray, interpolation = "nearest")
            plt.savefig(title)
    return gridboards

gridboards = make_gridboards(nx, ny, marker_size, between_markers,
                aruco_dict, num_gridboards, save_dir)


def plot_gridboard_axes(image_path, vectors, factor, gridboards=None):
    """
    Used to plot multiple gridboards, with one large set of axes per gridboard
    """
    dist_coeffs = vectors[str(factor)]['dist_coeffs']
    cam_matrix = vectors[str(factor)]['cam_matrix']
    nx = 2
    ny = 2
    marker_size = 0.025  # 25mm
    between_markers = 0.005  # 5mm
    markers_per_board = nx * ny
    length_of_axis = (marker_size + between_markers) * nx - between_markers
    aruco_dict = aruco.Dictionary_get(aruco.DICT_4X4_250)
    offset = 0
    if not gridboards:
        gridboards = [aruco.GridBoard_create(
            nx, ny, marker_size, between_markers, aruco_dict, firstMarker=offset
        )]
    print(image_path)
    frame = cv2.imread(image_path)
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    corners, ids, rejectedImgPoints = aruco.detectMarkers(gray, aruco_dict)
    
    all_rvecs = []
    all_tvecs = []
    frame_with_markers = aruco.drawDetectedMarkers(frame.copy(), corners, ids)
    
    #     print(f"ids={ids}")
    for i, board in enumerate(gridboards):
        # Go through all the corners / ids found in the image, and just select the
        # ones relevant to the current board
        ids_subset = ids.copy()
        corners_subset = corners.copy()
        for j, item in enumerate(ids):
            if item[0] not in [val[0] for val in board.ids]:
                np.delete(ids_subset, j)
                np.delete(corners_subset, j)
        # Reset the vectors
        rvec = np.ones(shape=(3, 1), dtype=float)
        tvec = np.ones(shape=(3, 1), dtype=float)
        # Estimate the Board's pose
        ret, rvec, tvec = aruco.estimatePoseBoard(
            corners_subset,
            ids_subset,
            board,
            cam_matrix,
            dist_coeffs,
            rvec,
            tvec
        )
        
        if ret == 0:  # if no gridboard was found
            print("Failed to get BoardPose")
            rvecs, tvecs, objPoints = aruco.estimatePoseSingleMarkers(
                corners,
                marker_size,
                cam_matrix,
                dist_coeffs)
            for i in range(len(tvecs)):
                frame_with_markers = cv2.drawFrameAxes(
                    frame_with_markers, cam_matrix,
                    dist_coeffs, rvecs[i], tvecs[i],
                    length_of_axis, thickness=2)
        
        else:
            frame_with_markers = cv2.drawFrameAxes(frame_with_markers, cam_matrix,
                                                   dist_coeffs, rvec, tvec,
                                                   length_of_axis, thickness=2)
            all_rvecs.append(rvec)
            all_tvecs.append(tvec)
    plt.figure(figsize=(12, 6))
    plt.imshow(frame_with_markers, interpolation="nearest")
    return all_rvecs, all_tvecs






def plot_marker_axes(image_path, vectors, factor):
    """
    Used to plot multiple individual markers
    """
    aruco_dict = aruco.Dictionary_get(aruco.DICT_4X4_250)
    dist_coeffs = vectors[str(factor)]['dist_coeffs']
    cam_matrix = vectors[str(factor)]['cam_matrix']
    marker_size = 0.025 # 25mm
    between_markers = 0.005 # 5mm
    length_of_axis = marker_size / 2

    img = cv2.imread(image_path)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    corners, ids, rejectedImgPoints = aruco.detectMarkers(gray, aruco_dict)
    frame_markers = aruco.drawDetectedMarkers(img.copy(), corners, ids)
    rvecs, tvecs, objPoints = aruco.estimatePoseSingleMarkers(
                                corners,
                                marker_size,
                                cam_matrix,
                                dist_coeffs)
    for i in range(len(tvecs)):
        frame_markers = cv2.drawFrameAxes(
            frame_markers, cam_matrix,
            dist_coeffs, rvecs[i], tvecs[i],
            length_of_axis, thickness=2)
    plt.figure(figsize=(12,6))
    plt.imshow(frame_markers, interpolation = "nearest")
    return rvecs, tvecs






# Variables
marker_size = 0.023 # 23mm
length_of_axis = marker_size / 2
image_paths = sorted(glob.glob("gridboards_x2_100/rotated/_20/*"))
image_path = image_paths[1]
factor = 0.2
dist_coeffs = vectors[str(factor)]['dist_coeffs']
cam_matrix = vectors[str(factor)]['cam_matrix']



# Processing
rvecs, tvecs = plot_gridboard_axes(image_path, vectors, factor, gridboards=gridboards)
rvecs2, tvecs2 = plot_marker_axes(image_path, vectors, factor)


rotation_matrix, _ = cv2.Rodrigues(rvecs[0])
M = np.linalg.inv(np.matmul(rotation_matrix, np.identity(3)))


# uses M and tvec0 for everything

def convert_to_2d(p):  # converts from camera/world coordinates to 2d plane coordinates
    return np.matmul(M, p - tvecs[0])


def convert_to_3d(p):  # converts from 2d plane coordinates to camera/world coordinates (used for debugging only)
    rotation_matrix, _ = cv2.Rodrigues(rvecs[0])
    t = np.matmul(rotation_matrix, p)
    return t + tvecs[0]


def rotation_unit_vector(rvec):  # converts from object coordinates to camera/world coordinates
    rotation_matrix, _ = cv2.Rodrigues(rvec)
    return np.matmul(rotation_matrix, np.asarray([0, 0.01, 0]).T)


def convert_rotation_unit_vector_to_2d(v):  # converts from camera/world coordinates to the 2d plane coordinates
    rotation_matrix, _ = cv2.Rodrigues(rvecs[0])
    return np.matmul(rotation_matrix, v)



converted_tvecs = [convert_to_2d(tvec.T) for tvec in tvecs2]

# visualise 2d plane
xs = [tvec[0][0] for tvec in converted_tvecs]
ys = [tvec[1][0] for tvec in converted_tvecs]
fig = plt.figure()
plt.axis('square')
plt.scatter(xs, ys)



rotation_unit_vectors = [rotation_unit_vector(rvec.T) for rvec in rvecs2]
converted_rotation_unit_vectors = [convert_rotation_unit_vector_to_2d(v) for v in rotation_unit_vectors]

# visualise rotation stuff
rotation_points = [t.reshape((1, 3)) + v.reshape((1, 3)) for t, v in zip(converted_tvecs,  converted_rotation_unit_vectors)]  # you don't actually need this, this is just for the visualisation
xs = [tvec[0][0] for tvec in rotation_points]
ys = [tvec[0][1] for tvec in rotation_points]
plt.scatter(xs, ys)
plt.show()

# calculate rotations
rotations = [np.arctan(v[0]/v[1]) for v in converted_rotation_unit_vectors]
print([r * 360/(2*np.pi) for r in rotations])
